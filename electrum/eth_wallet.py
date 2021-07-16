# Electrum - lightweight Bitcoin client
# Copyright (C) 2015 Thomas Voegtlin
#
# Permission is hereby granted, free of charge, to any person
# obtaining a copy of this software and associated documentation files
# (the "Software"), to deal in the Software without restriction,
# including without limitation the rights to use, copy, modify, merge,
# publish, distribute, sublicense, and/or sell copies of the Software,
# and to permit persons to whom the Software is furnished to do so,
# subject to the following conditions:
#
# The above copyright notice and this permission notice shall be
# included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
# EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
# MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
# NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
# BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
# ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
# CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

# Wallet classes:
#   - Imported_Wallet: imported addresses or single keys, 0 or 1 keystore
#   - Standard_Wallet: one HD keystore, P2PKH-like scripts
#   - Multisig_Wallet: several HD keystores, M-of-N OP_CHECKMULTISIG scripts

import abc
import decimal
import itertools
import json
import os
import random
import threading
import time
from typing import Dict, List, Optional, Sequence, Set, Tuple, Union

import eth_account
from eth_account._utils import transactions as eth_account_transactions
import eth_keys
import eth_utils
import hexbytes

from electrum import (
    bip32,
    crypto,
    i18n,
    invoices,
    keystore,
    logging,
    simple_config,
    storage,
    util,
    wallet_db,
)
from electrum_gui.android import helpers
from electrum_gui.common.basic.functional import text as text_utils
from electrum_gui.common.coin import manager as coin_manager
from electrum_gui.common.provider import manager as provider_manager

_logger = logging.get_logger(__name__)


class InternalAddressCorruption(Exception):
    def __str__(self):
        return i18n._(
            "Wallet file corruption detected. "
            "Please restore your wallet from seed, and compare the addresses in both files"
        )


class Abstract_Eth_Wallet(abc.ABC):
    """
    Wallet classes are created to handle various address generation methods.
    Completion states (watching-only, single account, no seed, etc) are handled inside classes.
    """

    LOGGING_SHORTCUT = 'w'
    max_change_outputs = 1
    # gap_limit_for_change = 10
    gap_limit_for_change = 1

    wallet_type: str

    def __init__(
        self, db: wallet_db.WalletDB, storage: Optional[storage.WalletStorage], *, config: simple_config.SimpleConfig
    ):
        if not db.is_ready_to_be_used_by_wallet():
            raise Exception("storage not ready to be used by Abstract_Wallet")

        self.config = config
        assert self.config is not None, "config must not be None"
        self.db = db
        self.name = None
        self.hide_type = False
        self.storage = storage
        self.storage_pw = None
        # load addresses needs to be called before constructor for sanity checks
        if self.db.get('wallet_type') is None:
            self.db.put('wallet_type', self.wallet_type)
        else:
            self.wallet_type = self.db.get("wallet_type")
        db.load_addresses(self.wallet_type)
        self.keystore = None  # type: Optional[keystore.KeyStore]  # will be set by load_keystore
        self._chain_code = None
        self._identity = None
        self.lock = threading.RLock()
        self.load_and_cleanup()
        # saved fields
        self.use_change = db.get('use_change', True)
        self.multiple_change = db.get('multiple_change', False)
        self.labels = db.get_dict('labels')
        self.fiat_value = db.get_dict('fiat_value')
        self.receive_requests = db.get_dict('payment_requests')  # type: Dict[str, invoices.Invoice]
        self.invoices = db.get_dict('invoices')  # type: Dict[str, invoices.Invoice]
        self._reserved_addresses = set(db.get('reserved_addresses', []))
        self._total_balance = {}  # Cache total balance for a short period of time
        self.calc_unused_change_addresses()
        # save wallet type the first time
        if self.db.get('address_index') is None:
            self.set_address_index(0)
        self.name = self.db.get("name")
        self._coin_price_cache = {}

        self._contracts = None

    @property
    def coin(self) -> str:
        # NOTE: this should be "chain_code" instead of "coin", however both
        # the upper-level caller and the lower-level db both use the term
        # "coin", just keep it the same here.
        if self._chain_code is None:
            chain_code = self.db.get("coin")
            if chain_code is None:
                chain_code = "eth"  # default to eth
                self.db.put("coin", chain_code)
            self._chain_code = chain_code
        return self._chain_code

    @coin.setter
    def coin(self, chain_code: str) -> None:
        if self._chain_code is None and self.db.get("coin") is None:
            self._chain_code = chain_code
            self.db.put("coin", chain_code)

    @property
    def identity(self) -> str:
        if self._identity is None:
            prefix = self.coin if self.coin != "eth" else ""
            # crypto.sha256 returns a bytes object
            self._identity = crypto.sha256(prefix + self.get_addresses()[0]).hex()
        return self._identity

    @property
    def contracts(self) -> dict:
        if self._contracts is None:
            self._contracts = {}
            chain_code = self.coin
            for token_address in self.db.get("contracts", {}).keys():
                try:
                    token = coin_manager.get_coin_by_token_address(chain_code, token_address, add_if_missing=True)
                except Exception as e:
                    _logger.exception(f"Error in recovering contracts. contract_addr: {token_address}, error: {e}")

                if token is None:
                    _logger.error(f"Error in recovering contract {token_address} on chain {chain_code}.")
                    continue

                self._contracts[token_address] = token.symbol

        return self._contracts

    def get_derivation_path(self, address):
        pass

    def check_customer_and_default_path(self):
        derivation_path = self.get_derivation_path(self.get_addresses()[0])
        wallet_path_list = derivation_path.split('/')
        default_path = helpers.get_default_path(self.coin, int(helpers.get_path_info(derivation_path, pos=1)))
        default_path_list = default_path.split('/')

        wallet_path_list.pop(-1)
        default_path_list.pop(-1)
        return wallet_path_list == default_path_list

    def ensure_storage(self, path: str) -> None:
        # create a storage.WalletStorage for the newly created wallet
        # called before self.save_db() or self.update_password()
        if self.storage is None:
            self.storage = storage.WalletStorage(path)
            if not self.storage.file_exists():
                return

        raise util.FileAlreadyExist()

    def set_address_index(self, index):
        self.address_index = index
        self.db.put("address_index", self.address_index)

    def get_address_index(self):
        return self.address_index

    def set_name(self, name):
        self.name = name
        self.db.put("name", self.name)

    def get_name(self):
        return (
            self.name if self.name != "" else '%s...%s' % (self.get_addresses()[0][0:6], self.get_addresses()[0][-6:])
        )

    def save_db(self):
        if self.storage and not self.hide_type:
            self.db.set_modified(True)
            self.db.write(self.storage)

    def save_backup(self):
        backup_dir = util.get_backup_dir(self.config)
        if backup_dir is None:
            return
        new_db = wallet_db.WalletDB(self.db.dump(), manual_upgrades=False)

        new_path = os.path.join(backup_dir, self.basename() + '.backup')
        new_storage = storage.WalletStorage(new_path)
        new_storage._encryption_version = self.storage._encryption_version
        new_storage.pubkey = self.storage.pubkey
        new_db.set_modified(True)
        new_db.write(new_storage)
        return new_path

    def stop(self):
        # super().stop()
        if any([ks.is_requesting_to_be_rewritten_to_wallet_file for ks in self.get_keystores()]):
            self.save_keystore()
        self.save_db()

    # def set_up_to_date(self, b):
    #     super().set_up_to_date(b)
    #     if b: self.save_db()

    # def clear_history(self):
    #     #super().clear_history()
    #     self.save_db()

    def pubkeys_to_address(self, public_key: str):
        return eth_keys.keys.PublicKey(bytes.fromhex(public_key)).to_checksum_address()

    def get_all_balance(self) -> Tuple[decimal.Decimal, Dict]:
        if (
            self._total_balance.get("time")
            and time.time() - self._total_balance["time"] <= 10  # Only cache for 10s
        ):
            return (
                self._total_balance["balance_info"][0],
                self._total_balance["balance_info"][1].copy()
            )

        chain_code = self.coin
        checksum_address = eth_utils.to_checksum_address(self.get_addresses()[0])
        try:
            raw_main_balance = provider_manager.get_balance(chain_code, checksum_address)
        except Exception:
            _logger.error(
                "Failed to get balance for main coin of chain %s address %s",
                chain_code,
                checksum_address
            )
            raw_main_balance = 0
        main_balance = decimal.Decimal(eth_utils.from_wei(raw_main_balance, "ether"))

        tokens_balance_info = {}
        for token_address in self.contracts.keys():
            try:
                erc_balance = provider_manager.get_balance(
                    chain_code,
                    checksum_address,
                    token_address=token_address
                )
            except Exception:
                _logger.error(
                    "Failed to get balance for token %s of chain %s address %s",
                    token_address,
                    chain_code,
                    checksum_address
                )
                erc_balance = 0

            tokens_balance_info[token_address.lower()] = erc_balance

        self._total_balance['balance_info'] = main_balance, tokens_balance_info
        self._total_balance['time'] = time.time()
        return main_balance, tokens_balance_info.copy()

    def get_all_token_address(self):
        return list(self.contracts.keys())

    def get_contract_symbols_with_address(self):
        return [
            {"coin": symbol, "address": address} for address, symbol in self.contracts.items()
        ]

    def add_contract_token(self, contract_symbol, contract_address):
        contract_address = eth_utils.to_checksum_address(contract_address)
        self.contracts[contract_address] = contract_symbol
        self.db.put("contracts", self.contracts)
        self.save_db()

    def delete_contract_token(self, contract_address):
        contract_address = eth_utils.to_checksum_address(contract_address)
        self.contracts.pop(contract_address, None)
        self.db.put("contracts", self.contracts)
        self.save_db()

    def load_and_cleanup(self):
        self.load_keystore()
        #  TODO check_sum_address

    #     super().load_and_cleanup()

    # def add_address(self, address):
    #     if not self.db.get_addr_history(address):
    #         self.db.history[address] = []
    #         self.set_up_to_date(False)
    # if self.synchronizer:
    #     self.synchronizer.add(address)

    @abc.abstractmethod
    def load_keystore(self) -> None:
        pass

    def diagnostic_name(self):
        return self.basename()

    def __str__(self):
        return self.basename()

    def get_master_public_key(self):
        return None

    def get_master_public_keys(self):
        return []

    def basename(self) -> str:
        return self.storage.basename() if self.storage else 'no name'

    def check_returned_address_for_corruption(func):
        def wrapper(self, *args, **kwargs):
            addr = func(self, *args, **kwargs)
            self.check_address_for_corruption(addr)
            return addr

        return wrapper

    def calc_unused_change_addresses(self) -> Sequence[str]:
        """Returns a list of change addresses to choose from, for usage in e.g. new transactions.
        The caller should give priority to earlier ones in the list.
        """
        with self.lock:
            # We want a list of unused change addresses.
            # As a performance optimisation, to avoid checking all addresses every time,
            # we maintain a list of "not old" addresses ("old" addresses have deeply confirmed history),
            # and only check those.
            if not hasattr(self, '_not_old_change_addresses'):
                self._not_old_change_addresses = self.get_change_addresses()
            self._not_old_change_addresses = [
                addr for addr in self._not_old_change_addresses if not self.address_is_old(addr)
            ]
            unused_addrs = [
                addr
                for addr in self._not_old_change_addresses
                if not self.is_used(addr) and not self.is_address_reserved(addr)
            ]
            return unused_addrs

    def is_deterministic(self) -> bool:
        return self.keystore.is_deterministic()

    # def set_label(self, name: str, text: str = None) -> bool:
    #     if not name:
    #         return False
    #     changed = False
    #     old_text = self.labels.get(name)
    #     if text:
    #         text = text.replace("\n", " ")
    #         if old_text != text:
    #             self.labels[name] = text
    #             changed = True
    #     else:
    #         if old_text is not None:
    #             self.labels.pop(name)
    #             changed = True
    #     if changed:
    #         run_hook('set_label', self, name, text)
    #     return changed

    # def import_labels(self, path):
    #     data = read_json_file(path)
    #     for key, value in data.items():
    #         self.set_label(key, value)
    #
    # def export_labels(self, path):
    #     write_json_file(path, self.labels)

    def set_fiat_value(self, txid, ccy, text, fx, value_sat):
        if not self.db.get_transaction(txid):
            return
        # since fx is inserting the thousands separator,
        # and not util, also have fx remove it
        text = fx.remove_thousands_separator(text)
        def_fiat = self.default_fiat_value(txid, fx, value_sat)
        formatted = fx.ccy_amount_str(def_fiat, commas=False)
        def_fiat_rounded = decimal.Decimal(formatted)
        reset = not text
        if not reset:
            try:
                text_dec = decimal.Decimal(text)
                text_dec_rounded = decimal.Decimal(fx.ccy_amount_str(text_dec, commas=False))
                reset = text_dec_rounded == def_fiat_rounded
            except Exception:
                # garbage. not resetting, but not saving either
                return False
        if reset:
            d = self.fiat_value.get(ccy, {})
            if d and txid in d:
                d.pop(txid)
            else:
                # avoid saving empty dict
                return True
        else:
            if ccy not in self.fiat_value:
                self.fiat_value[ccy] = {}
            self.fiat_value[ccy][txid] = text
        return reset

    def get_fiat_value(self, txid, ccy):
        fiat_value = self.fiat_value.get(ccy, {}).get(txid)
        try:
            return decimal.Decimal(fiat_value)
        except Exception:
            return

    def sign_transaction(self, *args, **kwargs) -> str:
        # TODO: change python-trezor and trezor plugin code to make this
        # work in the way of
        # electrum_gui.common.secret.interfaces.SignerInterface
        if self.is_watching_only():
            return

        # sign. start with ready keystores.
        sig_num = 0
        for k in sorted(self.get_keystores(), key=lambda ks: ks.ready_to_sign(), reverse=True):
            try:
                # if k.can_sign(tmp_tx):
                v, r, s = k.sign_eth_tx(*args, **kwargs)
            except BaseException as e:
                msg = str(e)
                print(f"wallet:L1510======={e}======")
                if isinstance(e, util.UserCancelled):
                    raise BaseException(util.UserCancel())
                if -1 != msg.find("Can't Pair With You Device When Sign tx"):
                    sig_num += 1
                    continue
                raise BaseException(e)

        r = eth_utils.big_endian_to_int(r)
        s = eth_utils.big_endian_to_int(s)
        unsigned_transaction_dict = {
            "nonce": args[1],
            "gasPrice": args[2],
            "gas": args[3],
            "to":  eth_utils.to_checksum_address(args[4]),
            "value": args[5],
            "chainId": kwargs["chain_id"],
        }
        data = kwargs.get("data")
        if data is not None:
            unsigned_transaction_dict["data"] = data
        signed_tx_hex = eth_account_transactions.encode_transaction(
            eth_account_transactions.serializable_unsigned_transaction_from_dict(
                unsigned_transaction_dict
            ),
            (v, r, s)
        ).hex()

        return eth_utils.add_0x_prefix(signed_tx_hex)

    def is_mine(self, address) -> bool:
        if not address:
            return False
        return bool(self.get_address_index(address))

    def is_change(self, address) -> bool:
        if not self.is_mine(address):
            return False
        return self.get_address_index(address)[0] == 1

    @abc.abstractmethod
    def get_address_path_str(self, address: str) -> Optional[str]:
        """Returns derivation path str such as "m/0/5" to address,
        or None if not applicable.
        """
        pass

    def export_private_key(self, address: str, password: Optional[str]) -> str:
        pass

    def export_private_key_for_path(self, path: Union[Sequence[int], str], password: Optional[str]) -> str:
        raise Exception("this wallet is not deterministic")

    @abc.abstractmethod
    def get_public_keys(self, address: str) -> Sequence[str]:
        pass

    def get_public_keys_with_deriv_info(
        self, address: str
    ) -> Dict[bytes, Tuple[keystore.KeyStoreWithMPK, Sequence[int]]]:
        """Returns a map: pubkey -> (keystore, derivation_suffix)"""
        return {}

    @abc.abstractmethod
    def get_receiving_addresses(self, *, slice_start=None, slice_stop=None) -> Sequence[str]:
        pass

    @abc.abstractmethod
    def get_change_addresses(self, *, slice_start=None, slice_stop=None) -> Sequence[str]:
        pass

    def dummy_address(self):
        # first receiving address
        return self.get_receiving_addresses(slice_start=0, slice_stop=1)[0]

    # def balance_at_timestamp(self, domain, target_timestamp):
    #     # we assume that get_history returns items ordered by block height
    #     # we also assume that block timestamps are monotonic (which is false...!)
    #     h = self.get_history(domain=domain)
    #     balance = 0
    #     for hist_item in h:
    #         balance = hist_item.balance
    #         if hist_item.tx_mined_status.timestamp is None or hist_item.tx_mined_status.timestamp > target_timestamp:
    #             return balance - hist_item.delta
    #     # return last balance
    #     return balance

    # def get_onchain_history(self, *, domain=None):
    #     monotonic_timestamp = 0
    #     for hist_item in self.get_history(domain=domain):
    #         monotonic_timestamp = max(monotonic_timestamp, (hist_item.tx_mined_status.timestamp or 999_999_999_999))
    #         yield {
    #             'txid': hist_item.txid,
    #             'fee_sat': hist_item.fee,
    #             'height': hist_item.tx_mined_status.height,
    #             'confirmations': hist_item.tx_mined_status.conf,
    #             'timestamp': hist_item.tx_mined_status.timestamp,
    #             'monotonic_timestamp': monotonic_timestamp,
    #             'incoming': True if hist_item.delta>0 else False,
    #             'bc_value': Satoshis(hist_item.delta),
    #             'bc_balance': Satoshis(hist_item.balance),
    #             'date': timestamp_to_datetime(hist_item.tx_mined_status.timestamp),
    #             'label': self.get_label(hist_item.txid),
    #             'txpos_in_block': hist_item.tx_mined_status.txpos,
    #         }

    # def get_label(self, tx_hash: str) -> str:
    #     return self.labels.get(tx_hash, '') or self.get_default_label(tx_hash)
    #
    # def get_default_label(self, tx_hash) -> str:
    #     if not self.db.get_txi_addresses(tx_hash):
    #         labels = []
    #         for addr in self.db.get_txo_addresses(tx_hash):
    #             label = self.labels.get(addr)
    #             if label:
    #                 labels.append(label)
    #         return ', '.join(labels)
    #     return ''

    def is_address_reserved(self, addr: str) -> bool:
        # note: atm 'reserved' status is only taken into consideration for 'change addresses'
        return addr in self._reserved_addresses

    def set_reserved_state_of_address(self, addr: str, *, reserved: bool) -> None:
        if not self.is_mine(addr):
            return
        with self.lock:
            if reserved:
                self._reserved_addresses.add(addr)
            else:
                self._reserved_addresses.discard(addr)
            self.db.put('reserved_addresses', list(self._reserved_addresses))

    def can_export(self):
        return not self.is_watching_only() and hasattr(self.keystore, 'get_private_key')

    # def address_is_old(self, address: str, *, req_conf: int = 3) -> bool:
    #     """Returns whether address has any history that is deeply confirmed.
    #     Used for reorg-safe(ish) gap limit roll-forward.
    #     """
    #     max_conf = -1
    #     h = self.db.get_addr_history(address)
    #     needs_spv_check = not self.config.get("skipmerklecheck", False)
    #     for tx_hash, tx_height in h:
    #         if needs_spv_check:
    #             tx_age = self.get_tx_height(tx_hash).conf
    #         else:
    #             if tx_height <= 0:
    #                 tx_age = 0
    #             else:
    #                 tx_age = self.get_local_height() - tx_height + 1
    #         max_conf = max(max_conf, tx_age)
    #     return max_conf >= req_conf

    def try_detecting_internal_addresses_corruption(self) -> None:
        pass

    def check_address_for_corruption(self, addr: str) -> None:
        pass

    def get_unused_addresses(self) -> Sequence[str]:
        domain = self.get_receiving_addresses()
        # TODO we should index receive_requests by id
        in_use_by_request = [
            k for k in self.receive_requests.keys() if self.get_request_status(k) != invoices.PR_EXPIRED
        ]
        in_use_by_request = set(in_use_by_request)
        return [addr for addr in domain if not self.is_used(addr) and addr not in in_use_by_request]

    @check_returned_address_for_corruption
    def get_unused_address(self) -> Optional[str]:
        """Get an unused receiving address, if there is one.
        Note: there might NOT be one available!
        """
        addrs = self.get_unused_addresses()
        if addrs:
            return addrs[0]

    @check_returned_address_for_corruption
    def get_receiving_address(self) -> str:
        """Get a receiving address. Guaranteed to always return an address."""
        unused_addr = self.get_unused_address()
        if unused_addr:
            return unused_addr
        domain = self.get_receiving_addresses()
        if not domain:
            raise Exception("no receiving addresses in wallet?!")
        choice = domain[0]
        for addr in domain:
            if not self.is_used(addr):
                if addr not in self.receive_requests.keys():
                    return addr
                else:
                    choice = addr
        return choice

    def create_new_address(self, for_change: bool = False):
        raise Exception("this wallet cannot generate new addresses")

    def import_address(self, address: str) -> str:
        raise Exception("this wallet cannot import addresses")

    def import_addresses(self, addresses: List[str], *, write_to_disk=True) -> Tuple[List[str], List[Tuple[str, str]]]:
        raise Exception("this wallet cannot import addresses")

    def delete_address(self, address: str) -> None:
        raise Exception("this wallet cannot delete addresses")

    @abc.abstractmethod
    def get_fingerprint(self):
        pass

    def can_import_privkey(self):
        return False

    def can_import_address(self):
        return False

    def can_delete_address(self):
        return False

    def has_password(self):
        return self.has_keystore_encryption() or self.has_storage_encryption()

    def can_have_keystore_encryption(self):
        return self.keystore and self.keystore.may_have_password()

    def get_available_storage_encryption_version(self) -> storage.StorageEncryptionVersion:
        """Returns the type of storage encryption offered to the user.

        A wallet file (storage) is either encrypted with this version
        or is stored in plaintext.
        """
        if isinstance(self.keystore, keystore.Hardware_KeyStore):
            return storage.StorageEncryptionVersion.XPUB_PASSWORD
        else:
            return storage.StorageEncryptionVersion.USER_PASSWORD

    def has_keystore_encryption(self):
        """Returns whether encryption is enabled for the keystore.

        If True, e.g. signing a transaction will require a password.
        """
        if self.can_have_keystore_encryption():
            return self.db.get('use_encryption', False)
        return False

    def has_storage_encryption(self):
        """Returns whether encryption is enabled for the wallet file on disk."""
        return self.storage and self.storage.is_encrypted()

    @classmethod
    def may_have_password(cls):
        return True

    def check_password(self, password, str_pw=None):
        if self.has_keystore_encryption():
            self.keystore.check_password(password, eth_status=True)
        if self.has_storage_encryption():
            if str_pw is not None:
                self.storage.check_password(str_pw)
            else:
                self.storage.check_password(self.storage_pw)

    def update_password(self, old_pw, new_pw, str_pw=None, *, encrypt_storage: bool = True):
        if old_pw is None and self.has_password():
            raise util.InvalidPassword()
        self.check_password(old_pw, str_pw=str_pw)
        if self.storage and str_pw is not None:
            if encrypt_storage:
                enc_version = self.get_available_storage_encryption_version()
            else:
                enc_version = storage.StorageEncryptionVersion.PLAINTEXT
            self.storage.set_password(str_pw, enc_version)
            self.storage_pw = str_pw
        # make sure next storage.write() saves changes
        self.db.set_modified(True)

        # note: Encrypting storage with a hw device is currently only
        #       allowed for non-multisig wallets. Further,
        #       Hardware_KeyStore.may_have_password() == False.
        #       If these were not the case,
        #       extra care would need to be taken when encrypting keystores.
        self._update_password_for_keystore(old_pw, new_pw)
        encrypt_keystore = self.can_have_keystore_encryption()
        self.db.set_keystore_encryption(bool(new_pw) and encrypt_keystore)
        self.save_db()

    @abc.abstractmethod
    def _update_password_for_keystore(self, old_pw: Optional[str], new_pw: Optional[str]) -> None:
        pass

    def sign_message(self, address: str, message: str, password: str = None):
        if self.is_watching_only():
            raise Exception("Watch-only wallet can't sign message")

        if message.startswith("0x"):
            message_bytes = bytes.fromhex(message[2:])
        else:
            message_bytes = message.encode()
            preamble = f"\x19Ethereum Signed Message:\n{len(message_bytes)}"
            message_bytes = preamble.encode() + message_bytes

        message_hash = eth_utils.keccak(message_bytes)

        if isinstance(self.keystore, keystore.Hardware_KeyStore):
            address_path = self.get_derivation_path(address)
            signature = self.keystore.sign_eth_message(address_path, message).hex()
        elif isinstance(self, (Standard_Eth_Wallet, Imported_Eth_Wallet)):
            signature = eth_account.Account.signHash(
                message_hash, self.get_account(address, password).privateKey
            ).signature.hex()
        else:
            raise Exception("Illegal Exception")

        return eth_utils.add_0x_prefix(text_utils.force_text(signature))

    def verify_message(self, address, message, sig):
        sig = bytes.fromhex(eth_utils.remove_0x_prefix(sig))
        return self.keystore.verify_eth_message(address, message, sig)

    def decrypt_message(self, pubkey: str, message, password) -> bytes:
        addr = self.pubkeys_to_address([pubkey])
        index = self.get_address_index(addr)
        return self.keystore.decrypt_message(index, message, password)

    # @abc.abstractmethod
    # def pubkeys_to_address(self, pubkeys: Sequence[str]) -> Optional[str]:
    #     pass

    def clear_coin_price_cache(self):
        self._coin_price_cache = {}

    def is_billing_address(self, addr):
        # overridden for TrustedCoin wallets
        return False

    @abc.abstractmethod
    def is_watching_only(self) -> bool:
        pass

    def get_keystore(self) -> Optional[keystore.KeyStore]:
        return self.keystore

    def get_keystores(self) -> Sequence[keystore.KeyStore]:
        return [self.keystore] if self.keystore else []

    @abc.abstractmethod
    def save_keystore(self):
        pass

    @abc.abstractmethod
    def has_seed(self) -> bool:
        pass

    @abc.abstractmethod
    def get_all_known_addresses_beyond_gap_limit(self) -> Set[str]:
        pass


class Simple_Eth_Wallet(Abstract_Eth_Wallet):
    # wallet with a single keystore

    def pubkeys_to_address(self, public_key: str):
        super().pubkeys_to_address(public_key)

    def is_watching_only(self):
        return self.keystore.is_watching_only()

    def _update_password_for_keystore(self, old_pw, new_pw):
        if self.keystore and self.keystore.may_have_password():
            self.keystore.update_password(old_pw, new_pw, eth_status=True)
            self.save_keystore()

    def save_keystore(self):
        self.db.put('keystore', self.keystore.dump())

    @abc.abstractmethod
    def get_public_key(self, address: str) -> Optional[str]:
        pass

    def get_public_keys(self, address: str) -> Sequence[str]:
        return [self.get_public_key(address)]


class Imported_Eth_Wallet(Simple_Eth_Wallet):
    # wallet made of imported addresses

    wallet_type = 'eth_imported'

    @classmethod
    def _from_addresses(cls, coin: str, config: simple_config.SimpleConfig, addresses: List[str]):
        db = wallet_db.WalletDB("", manual_upgrades=False)
        db.put("wallet_type", f"{coin}_imported")

        wallet = cls(db, None, config=config)
        wallet.coin = coin
        good_addrs, _bad_addrs = wallet.import_addresses(addresses, write_to_disk=False)
        # FIXME tell user about bad_inputs
        if not good_addrs:
            raise BaseException(i18n._("No address available."))
        return wallet

    @classmethod
    def from_pubkey_or_addresses(cls, coin: str, config: simple_config.SimpleConfig, pubkey_or_addresses: str):
        try:
            pubkey = eth_keys.keys.PublicKey.from_compressed_bytes(hexbytes.HexBytes(pubkey_or_addresses))
            addresses = [pubkey.to_address()]
        except Exception:
            addresses = pubkey_or_addresses.split()

        return cls._from_addresses(coin, config, addresses)

    @classmethod
    def from_privkeys(cls, coin: str, config: simple_config.SimpleConfig, privkeys: str):
        db = wallet_db.WalletDB("", manual_upgrades=False)
        db.put("keystore", keystore.Imported_KeyStore({}).dump())
        db.put("wallet_type", f"{coin}_imported")

        wallet = cls(db, None, config=config)
        wallet.coin = coin
        keys = keystore.get_eth_private_key(privkeys, allow_spaces_inside_key=False)
        good_addrs, _bad_keys = wallet.import_private_keys(keys, None, write_to_disk=False)
        # FIXME tell user about bad_inputs
        if not good_addrs:
            raise BaseException(i18n._("No private key available."))
        return wallet

    @classmethod
    def _create_customer_wallet(cls, ks, config, coin):
        db = wallet_db.WalletDB("", manual_upgrades=False)
        db.put("keystore", ks.dump())
        wallet = cls(db, None, config=config)
        wallet.coin = coin
        pubkey = eth_keys.keys.PublicKey.from_compressed_bytes(ks.get_pubkey_from_master_xpub())
        addr = eth_utils.to_checksum_address(pubkey.to_address())
        wallet.db.add_imported_address(addr, {'pubkey': str(pubkey)})
        return wallet

    @classmethod
    def from_xpub(
        cls, coin: str, config: simple_config.SimpleConfig, xpub: str, derivation: str, device_id: str, hw=False
    ):
        if hw:
            is_valid = keystore.is_bip32_key(xpub)
            if not is_valid:
                raise util.UnavailableXpub()
            ks = keystore.hardware_keystore(
                {
                    'type': 'hardware',
                    'hw_type': 'trezor',
                    'derivation': derivation,
                    'xpub': xpub,
                    'label': 'device_info.label',
                    'device_id': device_id,
                }
            )
        else:
            ks = keystore.from_master_key(xpub)

        return cls._create_customer_wallet(ks, config, coin)

    def get_device_info(self):
        if isinstance(self.keystore, keystore.Hardware_KeyStore):
            return self.keystore.get_device_info()

    @classmethod
    def from_seed(cls, coin: str, config: simple_config.SimpleConfig, seed: str, passphrase: str, derivation: str):
        ks = keystore.from_seed_or_bip39(seed, passphrase, derivation)
        return cls._create_customer_wallet(ks, config, coin)

    @classmethod
    def from_keystores(cls, coin: str, config: simple_config.SimpleConfig, keystores: str, password: str):
        try:
            privkeys = eth_account.Account.decrypt(keystores, password).hex()
        except (TypeError, KeyError, NotImplementedError, json.decoder.JSONDecodeError):
            raise util.InvalidKeystoreFormat()
        except Exception as e:
            raise util.InvalidPassword()
        return cls.from_privkeys(coin, config, privkeys)

    def __init__(self, db, storage, *, config):
        Abstract_Eth_Wallet.__init__(self, db, storage, config=config)

    def is_watching_only(self):
        return self.keystore is None

    def can_import_privkey(self):
        return bool(self.keystore)

    def load_keystore(self):
        self.keystore = keystore.load_keystore(self.db, 'keystore') if self.db.get('keystore') else None

    def save_keystore(self):
        self.db.put('keystore', self.keystore.dump())

    def can_import_address(self):
        return self.is_watching_only()

    def can_delete_address(self):
        return True

    def has_seed(self):
        if self.keystore is not None:
            return self.keystore.has_seed()

    def get_seed(self, password):
        if self.has_seed():
            return self.keystore.get_seed(password)

    def is_deterministic(self):
        return False

    def is_change(self, address):
        return False

    def get_all_known_addresses_beyond_gap_limit(self) -> Set[str]:
        return set()

    def get_fingerprint(self):
        return ''

    def get_addresses(self):
        # note: overridden so that the history can be cleared
        return self.db.get_imported_addresses()

    def export_private_key(self, address, password):
        pubk = self.get_public_key(address)
        prvk = ""
        if isinstance(self.keystore, keystore.Imported_KeyStore):
            prvk = self.keystore.get_eth_private_key(pubk, password)
        else:
            prvk = eth_utils.add_0x_prefix(self.keystore.get_master_private_key_info(password).hex())
        return prvk

    def get_account(self, address, password):
        private_key = self.export_private_key(address, password)
        print(f"get_account....{private_key}")
        return eth_account.Account.privateKeyToAccount(private_key)

    def export_keystore(self, address, password):
        prvk = self.export_private_key(address, password)
        encrypted_private_key = eth_account.Account.encrypt(prvk, password)
        return encrypted_private_key

    def get_receiving_addresses(self, **kwargs):
        return self.get_addresses()

    def get_change_addresses(self, **kwargs):
        return []

    def import_addresses(self, addresses: List[str], *, write_to_disk=True) -> Tuple[List[str], List[Tuple[str, str]]]:
        good_addr = []  # type: List[str]
        bad_addr = []  # type: List[Tuple[str, str]]
        for address in addresses:
            if not address or not eth_utils.is_address(address):
                bad_addr.append((address, i18n._('invalid address')))
                continue

            if self.db.has_imported_address(address):
                bad_addr.append((address, i18n._('address already in wallet')))
                continue
            good_addr.append(address)
            self.db.add_imported_address(address, {})
        if write_to_disk:
            self.save_db()
        return good_addr, bad_addr

    def import_address(self, address: str) -> str:
        good_addr, bad_addr = self.import_addresses([address])
        if good_addr and good_addr[0] == address:
            return address
        else:
            raise BaseException("Not checksumaddr")

    def is_mine(self, address) -> bool:
        if not address:
            return False
        return self.db.has_imported_address(address)

    def get_address_index(self, address) -> Optional[str]:
        # returns None if address is not mine
        return self.get_public_key(address)

    def get_address_path_str(self, address):
        return None

    def get_public_key(self, address) -> Optional[str]:
        x = self.db.get_imported_address(address)
        return x.get('pubkey') if x else None

    def import_private_keys(
        self, keys: List[str], password: Optional[str], *, write_to_disk=True
    ) -> Tuple[List[str], List[Tuple[str, str]]]:
        good_addr = []  # type: List[str]
        bad_keys = []  # type: List[Tuple[str, str]]
        for key in keys:
            try:
                pubkey = self.keystore.import_eth_privkey(key, password)
            except BaseException as e:
                bad_keys.append((key, i18n._('invalid private key') + f': {e}'))
                continue
            addr = eth_utils.to_checksum_address(pubkey.to_address())
            good_addr.append(addr)
            self.db.add_imported_address(addr, {'pubkey': pubkey.__str__()})
            # self.add_address(addr)
        self.save_keystore()
        if write_to_disk:
            self.save_db()
        return good_addr, bad_keys

    def import_private_key(self, key: str, password: Optional[str]) -> str:
        good_addr, bad_keys = self.import_private_keys([key], password=password)
        if good_addr:
            return good_addr[0]
        else:
            raise BaseException("Not checksumaddr")
            # raise BitcoinException(str(bad_keys[0][1]))

    def decrypt_message(self, pubkey: str, message, password) -> bytes:
        # this is significantly faster than the implementation in the superclass
        return self.keystore.decrypt_message(pubkey, message, password)

    #The return value is the full path
    def get_derivation_path(self, address):
        if self.keystore is None or isinstance(self.keystore, keystore.Imported_KeyStore):
            return ""
        else:
            return self.keystore.get_derivation_prefix()

class Deterministic_Eth_Wallet(Abstract_Eth_Wallet):
    def __init__(self, db, storage, *, config, index):
        self._ephemeral_addr_to_addr_index = {}  # type: Dict[str, Sequence[int]]
        Abstract_Eth_Wallet.__init__(self, db, storage, config=config)
        self.gap_limit = db.get('gap_limit', 1)
        # generate addresses now. note that without libsecp this might block
        # for a few seconds!
        self.synchronize(index)

    def has_seed(self):
        return self.keystore.has_seed()

    def get_addresses(self):
        # note: overridden so that the history can be cleared.
        # addresses are ordered based on derivation
        out = self.get_receiving_addresses()
        out += self.get_change_addresses()
        return out

    def get_receiving_addresses(self, *, slice_start=None, slice_stop=None):
        return self.db.get_receiving_addresses(slice_start=slice_start, slice_stop=slice_stop)

    def get_change_addresses(self, *, slice_start=None, slice_stop=None):
        return self.db.get_change_addresses(slice_start=slice_start, slice_stop=slice_stop)

    @util.profiler
    def try_detecting_internal_addresses_corruption(self):
        addresses_all = self.get_addresses()
        # sample 1: first few
        addresses_sample1 = addresses_all[:10]
        # sample2: a few more randomly selected
        addresses_rand = addresses_all[10:]
        addresses_sample2 = random.sample(addresses_rand, min(len(addresses_rand), 10))
        for addr_found in itertools.chain(addresses_sample1, addresses_sample2):
            self.check_address_for_corruption(addr_found)

    def check_address_for_corruption(self, addr):
        if addr and self.is_mine(addr):
            if addr != self.derive_address(*self.get_address_index(addr)):
                raise InternalAddressCorruption()

    def get_seed(self, password):
        return self.keystore.get_seed(password)

    def change_gap_limit(self, value):
        '''This method is not called in the code, it is kept for console use'''
        value = int(value)
        if value >= self.min_acceptable_gap():
            self.gap_limit = value
            self.db.put('gap_limit', self.gap_limit)
            self.save_db()
            return True
        else:
            return False

    def num_unused_trailing_addresses(self, addresses):
        k = 0
        for addr in addresses[::-1]:
            if self.db.get_addr_history(addr):
                break
            k += 1
        return k

    def min_acceptable_gap(self) -> int:
        # fixme: this assumes wallet is synchronized
        n = 0
        nmax = 0
        addresses = self.get_receiving_addresses()
        k = self.num_unused_trailing_addresses(addresses)
        for addr in addresses[0:-k]:
            if self.address_is_old(addr):
                n = 0
            else:
                n += 1
                nmax = max(nmax, n)
        return nmax + 1

    @abc.abstractmethod
    def derive_pubkeys(self, c: int, i: int) -> Sequence[str]:
        pass

    def derive_address(self, for_change: int, n: int) -> str:
        for_change = int(for_change)
        pubkeys = self.derive_pubkeys(for_change, n)
        return self.pubkeys_to_address(pubkeys)

    def export_private_key_for_path(self, path: Union[Sequence[int], str], password: Optional[str]) -> str:
        pass

    def get_public_keys_with_deriv_info(self, address: str):
        der_suffix = self.get_address_index(address)
        der_suffix = [int(x) for x in der_suffix]
        return {k.derive_pubkey(*der_suffix): (k, der_suffix) for k in self.get_keystores()}

    def create_new_address(self, for_change: bool = False, index: int = 0):
        assert type(for_change) is bool
        with self.lock:
            address = self.derive_address(int(for_change), index)
            self.db.add_receiving_address(address, index=index)
            self.set_address_index(index)
            # self.add_address(address)
            if for_change:
                # note: if it's actually "old", it will get filtered later
                self._not_old_change_addresses.append(address)
            return address

    def synchronize_sequence(self, for_change, index=0):
        limit = self.gap_limit_for_change if for_change else self.gap_limit
        while True:
            num_addr = self.db.num_change_addresses() if for_change else self.db.num_receiving_addresses()
            if num_addr < limit:
                self.create_new_address(for_change, index=index)
                continue
            break
            # if for_change:
            #     last_few_addresses = self.get_change_addresses(slice_start=-limit)
            # else:
            #     last_few_addresses = self.get_receiving_addresses(slice_start=-limit)
            # if any(map(self.address_is_old, last_few_addresses)):
            #     self.create_new_address(for_change)
            # else:
            #     break

    def synchronize(self, index):
        with self.lock:
            self.synchronize_sequence(False, index)
            self.synchronize_sequence(True, index)

    def get_all_known_addresses_beyond_gap_limit(self):
        # note that we don't stop at first large gap
        found = set()

        def process_addresses(addrs, gap_limit):
            rolling_num_unused = 0
            for addr in addrs:
                if self.db.get_addr_history(addr):
                    rolling_num_unused = 0
                else:
                    if rolling_num_unused >= gap_limit:
                        found.add(addr)
                    rolling_num_unused += 1

        process_addresses(self.get_receiving_addresses(), self.gap_limit)
        process_addresses(self.get_change_addresses(), self.gap_limit_for_change)
        return found

    def get_address_index(self, address) -> Optional[Sequence[int]]:
        return self.db.get_address_index(address) or self._ephemeral_addr_to_addr_index.get(address)

    def get_address_path_str(self, address):
        intpath = self.get_address_index(address)
        if intpath is None:
            return None
        return bip32.convert_bip32_intpath_to_strpath(intpath)

    def get_master_public_keys(self):
        return [self.get_master_public_key()]

    def get_fingerprint(self):
        return self.get_master_public_key()

    def get_device_info(self):
        return self.keystore.get_device_info()


class Simple_Eth_Deterministic_Wallet(Simple_Eth_Wallet, Deterministic_Eth_Wallet):

    """ Deterministic Wallet with a single pubkey per address """

    def __init__(self, db, storage, *, config, index):
        Deterministic_Eth_Wallet.__init__(self, db, storage, config=config, index=index)

    def get_public_key(self, address):
        sequence = self.get_address_index(address)
        pubkeys = self.derive_pubkeys(*sequence)
        return pubkeys[0]

    def load_keystore(self):
        self.keystore = keystore.load_keystore(self.db, 'keystore')

    def get_master_public_key(self):
        return self.keystore.get_master_public_key()

    def derive_pubkeys(self, c, i, compressed=False):
        return [self.keystore.derive_pubkey(c, i, compressed).hex()]


class Standard_Eth_Wallet(Simple_Eth_Deterministic_Wallet):
    wallet_type = 'eth_standard'

    @classmethod
    def _from_keystore(cls, coin: str, index: int, config: simple_config.SimpleConfig, keystore: keystore.KeyStore):
        db = wallet_db.WalletDB("", manual_upgrades=False)
        db.put("keystore", keystore.dump())
        db.put("wallet_type", f"{coin}_standard")

        wallet = cls(db, None, config=config, index=index)
        wallet.coin = coin
        return wallet

    @classmethod
    def from_seed_or_bip39(
        cls, coin: str, index: int, config: simple_config.SimpleConfig, seed: str, passphrase: str, derivation: str
    ):
        ks = keystore.from_seed_or_bip39(seed, passphrase, derivation)
        return cls._from_keystore(coin, index, config, ks)

    def __init__(self, db, storage, *, config, index=0):
        Simple_Eth_Deterministic_Wallet.__init__(self, db, storage, config=config, index=index)
        self.hd_main_eth_address = ''

    def pubkeys_to_address(self, public_key: str):
        return eth_keys.keys.PublicKey(bytes.fromhex(public_key)).to_checksum_address()

    def get_keystore_by_address(self, address, password):
        privatekey = self.get_private_key(address, password)
        encrypted_private_key = eth_account.Account.encrypt(privatekey, password)
        return json.dumps(encrypted_private_key)

    def load_keystore(self):
        self.keystore = keystore.load_keystore(self.db, 'keystore') if self.db.get('keystore') else None

    def save_keystore(self):
        self.db.put('keystore', self.keystore.dump())

    def export_private_key_for_path(self, path: Union[Sequence[int], str], password: Optional[str]) -> bytes:
        if isinstance(path, str):
            path = bip32.convert_bip32_path_to_list_of_uint32(path)
        # pk, compressed = self.keystore.get_private_key(path, password)
        ck, pk = self.keystore.get_keypair(path, password)
        return pk.hex()

    def get_account(self, address, password):
        private_key = self.get_private_key(address, password)
        print(f"get_account....{private_key}")
        return eth_account.Account.privateKeyToAccount(private_key)

    def get_main_eth_address(self):
        self.hd_main_eth_address = self.get_addresses()[0]
        return self.hd_main_eth_address

    def get_addresses(self):
        out = self.get_receiving_addresses()
        return out

    def derive_address(self, for_change: int, n: int) -> str:
        for_change = int(for_change)
        pubkeys = self.derive_pubkeys(for_change, n, compressed=False)
        return self.pubkeys_to_address(pubkeys[0][2:])

    def synchronize(self, index=0):
        with self.lock:
            self.synchronize_sequence(False, index)

    def export_private_key(self, address, password):
        return self.get_private_key(address, password=password)

    def export_keystore(self, address, password):
        prvk = self.export_private_key(address, password)
        encrypted_private_key = eth_account.Account.encrypt(prvk, password)
        return encrypted_private_key

    def get_private_key(self, address, password):
        path = self.db.get_address_index(address)
        private_key = self.export_private_key_for_path(path, password)
        return "0x%s" % private_key

    def get_derivation_path(self, address):
        derivation = self.keystore.get_derivation_prefix()
        deriv_suffix = self.get_address_index(address)
        return "%s/%d/%d" % (derivation, *deriv_suffix) if derivation is not None else ""


wallet_types = ['standard', 'multisig', 'imported']


def register_wallet_type(category):
    wallet_types.append(category)


wallet_constructors = {
    'eth_standard': Standard_Eth_Wallet,
    'eth_xpub': Standard_Eth_Wallet,
    'eth_imported': Imported_Eth_Wallet,
    'bsc_standard': Standard_Eth_Wallet,
    'bsc_xpub': Standard_Eth_Wallet,
    'bsc_imported': Imported_Eth_Wallet,
    'heco_standard': Standard_Eth_Wallet,
    'heco_xpub': Standard_Eth_Wallet,
    'heco_imported': Imported_Eth_Wallet,
    'okt_standard': Standard_Eth_Wallet,
    'okt_xpub': Standard_Eth_Wallet,
    'okt_imported': Imported_Eth_Wallet,
}


def register_constructor(wallet_type, constructor):
    wallet_constructors[wallet_type] = constructor


# former WalletFactory
class Eth_Wallet(object):
    def __new__(
        self, db: 'wallet_db.WalletDB', storage: Optional[storage.WalletStorage], *, config: simple_config.SimpleConfig
    ):
        wallet_type = db.get('wallet_type')
        if wallet_type.endswith("imported"):
            WalletClass = Imported_Eth_Wallet
        else:
            WalletClass = Standard_Eth_Wallet
        wallet = WalletClass(db, storage, config=config)
        return wallet

    @staticmethod
    def wallet_class(wallet_type):
        # if multisig_type(wallet_type):
        #     return Multisig_Wallet
        if wallet_type in wallet_constructors:
            return wallet_constructors[wallet_type]
        raise util.WalletFileException("Unknown wallet type: " + str(wallet_type))
