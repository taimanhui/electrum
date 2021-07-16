from __future__ import absolute_import, division, print_function

import asyncio
import copy
import itertools
import json
import logging
import math
import os
import random
import string
import threading
import urllib.parse
from code import InteractiveConsole
from contextlib import contextmanager
from decimal import Decimal
from operator import attrgetter
from os.path import exists, join
from typing import Any, Dict, List, Literal, Optional, Tuple

import eth_abi
import eth_utils
from eth_account import account as eth_account_account
from eth_keys import keys
from hexbytes import HexBytes
from mnemonic import Mnemonic
from trezorlib.customer_ui import CustomerUI

from electrum import MutiBase, bitcoin, commands, constants, daemon, ecc, keystore
from electrum import mnemonic as electrum_mnemonic
from electrum import paymentrequest, simple_config, util
from electrum.address_synchronizer import TX_HEIGHT_FUTURE, TX_HEIGHT_LOCAL
from electrum.bip32 import BIP32Node
from electrum.bip32 import convert_bip32_path_to_list_of_uint32 as parse_path
from electrum.bip32 import get_uncompressed_key
from electrum.bitcoin import COIN
from electrum.constants import read_json
from electrum.eth_wallet import Abstract_Eth_Wallet, Eth_Wallet, Imported_Eth_Wallet, Standard_Eth_Wallet
from electrum.i18n import _, set_language
from electrum.interface import ServerAddr
from electrum.keystore import (
    Hardware_KeyStore,
    Imported_KeyStore,
    bip44_derivation,
    bip44_eth_derivation,
    purpose48_derivation,
)
from electrum.mnemonic import Wordlist
from electrum.network import TxBroadcastError
from electrum.plugin import Plugins
from electrum.storage import WalletStorage
from electrum.transaction import PartialTransaction, PartialTxOutput, SerializationError, Transaction, tx_from_any
from electrum.util import (
    DerivedWalletLimit,
    FailedGetTx,
    FailedToSwitchWallet,
    Fiat,
    FileAlreadyExist,
    InvalidAddressURI,
    InvalidBip39Seed,
    InvalidPassword,
    NotSupportExportSeed,
    Ticker,
    UnavaiableHdWallet,
    UnavailableBtcAddr,
    UnavailableEthAddr,
    UnsupportedCurrencyCoin,
    UserCancel,
    bfh,
    create_and_start_event_loop,
    format_time,
)
from electrum.util import user_dir as get_dir
from electrum.wallet import Imported_Wallet, Standard_Wallet, Wallet
from electrum.wallet_db import WalletDB
from electrum_gui.android import hardware, helpers, wallet_context
from electrum_gui.common import the_begging
from electrum_gui.common.basic import api, exceptions
from electrum_gui.common.basic.functional import json_encoders
from electrum_gui.common.basic.functional import text as text_utils
from electrum_gui.common.basic.orm import database as orm_database
from electrum_gui.common.basic.request.exceptions import ResponseException
from electrum_gui.common.basic.request.restful import RestfulRequest
from electrum_gui.common.coin import codes
from electrum_gui.common.coin import manager as coin_manager
from electrum_gui.common.conf import chains as chains_config
from electrum_gui.common.price import manager as price_manager
from electrum_gui.common.provider import data as provider_data
from electrum_gui.common.provider import exceptions as provider_exceptions
from electrum_gui.common.provider import provider_manager
from electrum_gui.common.provider.chains.eth.clients import geth as geth_client
from electrum_gui.common.secret import manager as secret_manager
from electrum_gui.common.transaction import data as transaction_data
from electrum_gui.common.transaction import manager as transaction_manager
from electrum_gui.common.wallet import bip44
from electrum_gui.common.wallet import manager as wallet_manager

from ..common.basic.functional.require import require
from .create_wallet_info import CreateWalletInfo
from .derived_info import DerivedInfo
from .migrating import GeneralWallet, is_coin_migrated
from .tx_db import TxDb

log_info = logging.getLogger(__name__)

IS_ANDROID = True
if "iOS_DATA" in os.environ:
    from .ioscallback import CallHandler

    IS_ANDROID = False

PURPOSE_POS = 1
ACCOUNT_POS = 3
INDEX_POS = 5
BTC_BLOCK_INTERVAL_TIME = 10
DEFAULT_ADDR_TYPE = 49
ticker = None

PURPOSE_TO_ADDRESS_TYPE = {
    44: 'p2pkh',
    49: 'p2wpkh-p2sh',
    84: 'p2wpkh',
}


class AndroidConsole(InteractiveConsole):
    """`interact` must be run on a background thread, because it blocks waiting for input."""

    def __init__(self, app, cmds):
        namespace = dict(c=cmds, context=app)
        namespace.update({name: CommandWrapper(cmds, name) for name in all_commands})
        namespace.update(help=Help())
        InteractiveConsole.__init__(self, locals=namespace)

    def interact(self):
        try:
            InteractiveConsole.interact(
                self,
                banner=(
                    _("WARNING!")
                    + "\n"
                    + _(
                        "Do not enter code here that you don't understand. Executing the wrong "
                        "code could lead to your coins being irreversibly lost."
                    )
                    + "\n"
                    + "Type 'help' for available commands and variables."
                ),
            )
        except SystemExit:
            pass


class CommandWrapper:
    def __init__(self, cmds, name):
        self.cmds = cmds
        self.name = name

    def __call__(self, *args, **kwargs):
        return getattr(self.cmds, self.name)(*args, **kwargs)


class Help:
    def __repr__(self):
        return self.help()

    def __call__(self, *args):
        print(self.help(*args))

    def help(self, name_or_wrapper=None):
        if name_or_wrapper is None:
            return (
                "Commands:\n"
                + "\n".join(f"  {cmd}" for name, cmd in sorted(all_commands.items()))
                + "\nType help(<command>) for more details.\n"
                "The following variables are also available: "
                "c.config, c.daemon, c.network, c.wallet, context"
            )
        else:
            if isinstance(name_or_wrapper, CommandWrapper):
                cmd = all_commands[name_or_wrapper.name]
            else:
                cmd = all_commands[name_or_wrapper]
            return f"{cmd}\n{cmd.description}"


def _get_chain_affinity(chain_code):
    chain_info = coin_manager.get_chain_info(chain_code)
    return chain_info.chain_affinity


# Adds additional commands which aren't available over JSON RPC.
class AndroidCommands(commands.Commands):
    _recovery_flag = True

    def __init__(self, android_id=None, config=None, user_dir=None, callback=None, chain_type="mainnet"):
        self.asyncio_loop, self._stop_loop, self._loop_thread = create_and_start_event_loop()
        self.config = config or simple_config.SimpleConfig({"auto_connect": True})
        if user_dir is None:
            self.user_dir = get_dir()
        else:
            self.user_dir = user_dir
        fd = daemon.get_file_descriptor(self.config)
        if not fd:
            raise BaseException(("Daemon already running, Don't start the wallet repeatedly"))
        set_language(self.config.get("language", "zh_CN"))

        # Initialize here rather than in start() so the DaemonModel has a chance to register
        # its callback before the daemon threads start.
        self.daemon = daemon.Daemon(self.config, fd)
        self.txdb = TxDb(path=self._tx_list_path(name="tx_info.db"))
        self.network = self.daemon.network
        self.daemon_running = False
        self.wizard = None
        self.plugin = Plugins(self.config, "cmdline")
        self.label_plugin = self.plugin.load_plugin("labels")
        self.label_flag = self.config.get("use_labels", False)
        self.callbackIntent = None
        self.hd_wallet = None
        self.check_pw_wallet = None
        self.wallet = None
        self.client = None
        self.recovery_wallets = {}
        self.path = ""
        self.replace_wallet_info = {}
        self._set_base_uint("BTC")
        ran_str = self.config.get("ra_str", None)
        if ran_str is None:
            ran_str = "".join(random.sample(string.ascii_letters + string.digits, 8))
            self.config.set_key("ra_str", ran_str)
        self.android_id = android_id + ran_str
        self.wallet_context = wallet_context.WalletContext(self.config, self.user_dir)
        if self.network:
            interests = [
                "wallet_updated",
                "network_updated",
                "blockchain_updated",
                "status",
                "new_transaction",
                "verified",
                "set_server_status",
            ]
            util.register_callback(self.on_network_event, interests)
            util.register_callback(self.on_fee, ["fee"])
            # self.network.register_callback(self.on_fee_histogram, ['fee_histogram'])
            util.register_callback(self.on_quotes, ["on_quotes"])
            util.register_callback(self.on_history, ["on_history"])
        self.fiat_unit = self.daemon.fx.ccy if self.daemon.fx.is_enabled() else ""
        self.decimal_point = self.config.get("decimal_point", util.DECIMAL_POINT_DEFAULT)
        self.hw_info = {}
        for k, v in util.base_units_inverse.items():
            if k == self.decimal_point:
                self.base_unit = v
        self.old_history_len = 0
        self.old_history_info = []
        self.num_zeros = int(self.config.get("num_zeros", 0))
        self.config.set_key("log_to_file", True, save=True)
        self.rbf = self.config.get("use_rbf", True)
        self.ccy = self.daemon.fx.get_currency()
        self.pre_balance_info = ""
        self.addr_index = 0
        self.rbf_tx = ""
        self.m = 0
        self.n = 0
        self._tokens_dict_of_chain = {}
        self.config.set_key("auto_connect", True, True)
        global ticker
        ticker = Ticker(5.0, self.update_status)
        ticker.start()
        if IS_ANDROID:
            if callback is not None:
                self.set_callback_fun(callback)
        else:
            self.my_handler = CallHandler.alloc().init()
            self.set_callback_fun(self.my_handler)
        self.start_daemon()
        self.get_block_info()
        the_begging.initialize()

        self.trezor_manager = hardware.TrezorManager(self.plugin)

        self._load_all_wallet()

    def __getattr__(self, name):
        if name in self.trezor_manager.exposed_commands:
            return getattr(self.trezor_manager, name)

        raise AttributeError

    # not needed in the future
    def set_language(self, language):
        """
        Set the language of error messages displayed to users
        :param language: zh_CN/en_UK as string
        """
        set_language(language)
        self.config.set_key("language", language)

    # BEGIN commands from the argparse interface.
    def stop_loop(self):
        self.asyncio_loop.call_soon_threadsafe(self._stop_loop.set_result, 1)
        self._loop_thread.join(timeout=1)

    def on_fee(self, event, *arg):
        try:
            self.fee_status = self.config.get_fee_status()
        except BaseException as e:
            raise e

    # def on_fee_histogram(self, *args):
    #     self.update_history()

    def on_quotes(self, d):
        self.update_status()

    def on_history(self, d):
        if self.wallet:
            self.wallet.clear_coin_price_cache()
        # self.update_history()

    def update_status(self):
        if self.wallet is None:
            return

        coin = self.wallet.coin
        address = self.wallet.get_addresses()[0]
        out = dict()

        chain_affinity = _get_chain_affinity(coin)

        if is_coin_migrated(coin):
            main_balance_info, contracts_balance_info, sum_fiat = self._get_general_wallet_all_balance(self.wallet)
            out = main_balance_info
            out["tokens"] = contracts_balance_info
            out["sum_fiat"] = f"{self.daemon.fx.ccy_amount_str(sum_fiat, True)} {self.ccy}"
            out["coin_asset"] = (
                self._fill_balance_info_with_coin(sum_fiat, coin)
                if sum_fiat != Decimal(0)
                else main_balance_info["balance"]
            )

            out["name"] = self.wallet.identity
        elif chain_affinity == "eth":  # eth base
            main_balance_info, contracts_balance_info, sum_fiat = self._get_eth_wallet_all_balance(self.wallet)
            out = main_balance_info
            out["tokens"] = contracts_balance_info
            out["sum_fiat"] = f"{self.daemon.fx.ccy_amount_str(sum_fiat, True)} {self.ccy}"
            out["coin_asset"] = (
                self._fill_balance_info_with_coin(sum_fiat, coin)
                if sum_fiat != Decimal(0)
                else main_balance_info["balance"]
            )

            out["name"] = self.wallet.identity
        elif (
            self.network
            and self.network.is_connected()
            and self.network.get_server_height() != 0
            and self.wallet.up_to_date
            and chain_affinity == "btc"
        ):  # btc
            c, u, x = self.wallet.get_balance()
            balance = c + u
            fiat = Decimal(balance) / COIN * price_manager.get_last_price(coin, self.ccy)
            fiat_str = f"{self.daemon.fx.ccy_amount_str(fiat, True)} {self.ccy}"

            # main balance info
            out["coin"] = "btc"
            out["address"] = address
            out["icon"] = self._get_icon_by_token(coin)
            out["balance"] = self.format_amount(balance)
            out["fiat"] = fiat_str

            out["tokens"] = []
            out["sum_fiat"] = fiat_str
            out["coin_asset"] = out["balance"]
            out["name"] = self.wallet.identity

            if u:
                out["unconfirmed"] = self.format_amount(u, is_diff=True).strip()
            if x:
                out["unmatured"] = self.format_amount(x, is_diff=True).strip()

        if out and self.callbackIntent is not None:
            self.callbackIntent.onCallback("update_status=%s" % json.dumps(out, cls=json_encoders.DecimalEncoder))

    def get_remove_flag(self, tx_hash):
        height = self.wallet.get_tx_height(tx_hash).height
        if height in [TX_HEIGHT_FUTURE, TX_HEIGHT_LOCAL]:
            return True
        else:
            return False

    def remove_local_tx(self, delete_tx):
        """
        :param delete_tx: tx_hash that you need to delete
        :return :
        """
        try:
            to_delete = {delete_tx}
            to_delete |= self.wallet.get_depending_transactions(delete_tx)
            for tx in to_delete:
                self.wallet.remove_transaction(tx)
                self.delete_tx(tx)
            self.wallet.save_db()
        except BaseException as e:
            raise e
        # need to update at least: history_list, utxo_list, address_list
        # self.parent.need_update.set()

    def delete_tx(self, hash):
        try:
            if self.label_flag and self.wallet.wallet_type != "standard":
                self.label_plugin.push_tx(self.wallet, "deltx", hash)
        except BaseException as e:
            if e != "Could not decode:":
                log_info.info("push_tx delete_tx error {}.".format(e))
            pass

    def get_wallet_info(self):
        wallet_info = {}
        wallet_info["balance"] = self.balance
        wallet_info["fiat_balance"] = self.fiat_balance
        wallet_info["name"] = self.wallet.get_name()
        return json.dumps(wallet_info)

    def on_network_event(self, event, *args):
        if event == "set_server_status" and self.callbackIntent is not None:
            self.callbackIntent.onCallback("set_server_status=%s" % args[0])
        elif event in ("network_updated",):
            # TODO: Update the list of available servers
            pass
        elif event in (
            "wallet_updated",
            "blockchain_updated",
            "status",
            "new_transaction",
            "verified",
        ):
            self.update_status()

    def daemon_action(self):
        self.daemon_running = True
        self.daemon.run_daemon()

    def start_daemon(self):
        t1 = threading.Thread(target=self.daemon_action)
        t1.setDaemon(True)
        t1.start()

    def status(self):
        """Get daemon status"""
        try:
            self._assert_daemon_running()
        except Exception as e:
            raise BaseException(e)
        return self.daemon.run_daemon({"subcommand": "status"})

    def stop(self):
        """Stop the daemon"""
        try:
            self._assert_daemon_running()
        except Exception as e:
            raise BaseException(e)
        self.daemon.stop()
        self.daemon_running = False
        self.stop_loop()
        self.plugin.stop()
        global ticker
        ticker.cancel()
        the_begging.terminate()

    def set_hd_wallet(self, wallet_obj):
        if self.hd_wallet is None:
            self.hd_wallet = wallet_obj

    def load_wallet(self, name, password=None):  # noqa
        """
        load an wallet
        :param name: wallet name as a string
        :param password: the wallet password as a string
        :return:
        """
        try:
            self._assert_daemon_running()
        except Exception as e:
            raise BaseException(e)
        path = self._wallet_path(name)
        wallet = self.daemon.get_wallet(path)
        if not wallet:
            storage = WalletStorage(path)
            if not storage.file_exists():
                # (_("Your {} were successfully imported").format(title))
                raise BaseException(_("Failed to load file {}".format(path)))
            if storage.is_encrypted():
                if not password:
                    raise InvalidPassword()
                storage.decrypt(password)
            db = WalletDB(storage.read(), manual_upgrades=False)
            if db.requires_split():
                return
            if db.requires_upgrade():
                return
            if db.get_action():
                return
            try:
                wallet_type = db.data["wallet_type"]
                coin = db.data["coin"]
            except Exception:
                wallet_type = self.wallet_context.get_wallet_type_by_id(name)
                if wallet_type:
                    coin = wallet_context.split_coin_from_wallet_type(wallet_type)
                else:
                    return
            chain_affinity = _get_chain_affinity(coin)
            if is_coin_migrated(coin):
                wallet = GeneralWallet(db, storage, self.config)
            elif chain_affinity == "btc":
                wallet = Wallet(db, storage, config=self.config)
                wallet.start_network(self.network)
            elif chain_affinity == "eth":
                if (
                    "importe" in wallet_type
                    or "private" in wallet_type
                    or "watch" in wallet_type
                    or "customer" in wallet_type
                ):
                    wallet = Eth_Wallet(db, storage, config=self.config)
                else:
                    index = db.data.get("address_index", 0)
                    wallet = Standard_Eth_Wallet(db, storage, config=self.config, index=index)
            else:
                raise UnsupportedCurrencyCoin()
            if self.wallet_context.is_hd(name):
                self.set_hd_wallet(wallet)
                bip39_derivation = wallet.get_derivation_path(wallet.get_addresses()[0])
                self.update_devired_wallet_info(
                    bip39_derivation,
                    self.get_hd_wallet_encode_seed(
                        coin=wallet.coin, purpose=helpers.get_path_info(bip39_derivation, PURPOSE_POS)
                    ),
                    wallet.name,
                    wallet.coin,
                )
            self.daemon.add_wallet(wallet)
        return wallet

    def close_wallet(self, name=None):
        """Close a wallet"""
        try:
            self._assert_daemon_running()
        except Exception as e:
            raise BaseException(e)
        self.daemon.stop_wallet(self._wallet_path(name))

    def set_syn_server(self, flag):
        """
        Enable/disable sync server
        :param flag: flag as bool
        :return: raise except if error
        """
        try:
            self.label_flag = flag
            self.config.set_key("use_labels", bool(flag))
            if (
                self.label_flag
                and self.wallet
                and self.wallet.wallet_type != "btc-standard"
                and self.wallet.wallet_type != "eth-standard"
            ):
                self.label_plugin.load_wallet(self.wallet)
        except Exception as e:
            raise BaseException(e)

    def set_callback_fun(self, callbackIntent):
        self.callbackIntent = callbackIntent

    def set_multi_wallet_info(self, name, m, n):
        try:
            self._assert_daemon_running()
        except Exception as e:
            raise BaseException(e)
        if self.wizard is not None:
            self.wizard = None
        self.wizard = MutiBase.MutiBase(self.config)
        path = self._wallet_path(name)
        self.wizard.set_multi_wallet_info(path, m, n)
        self.m = m
        self.n = n

    def _get_hw_derivation(self, account_id=0, type=84, coin="btc"):
        if self.wizard.wallet_type == 'multisig':
            return purpose48_derivation(0, xtype='p2wsh')
            # derivation = bip44_derivation(0, bip43_purpose=48)
        else:
            if 'btc' == coin:
                derivation = bip44_derivation(account_id, bip43_purpose=type)
            else:
                derivation = bip44_eth_derivation(account_id)
                derivation = util.get_keystore_path(derivation)
            return derivation

    def add_xpub(self, xpub, device_id=None, derivation=None):
        try:
            self._assert_daemon_running()
            self._assert_wizard_isvalid()
            if BIP32Node.from_xkey(xpub).xtype != "p2wsh" and self.n >= 2:
                xpub = BIP32Node.get_p2wsh_from_other(xpub)
            self.wizard.restore_from_xpub(xpub, device_id, derivation)
        except Exception as e:
            raise BaseException(e)

    def delete_xpub(self, xpub):
        """
        Delete xpub when create multi-signature wallet
        :param xpub: WIF pubkey
        :return:
        """
        try:
            self._assert_daemon_running()
            self._assert_wizard_isvalid()
            self.wizard.delete_xpub(xpub)
        except Exception as e:
            raise BaseException(e)

    def get_keystores_info(self):
        try:
            self._assert_daemon_running()
            self._assert_wizard_isvalid()
            ret = self.wizard.get_keystores_info()
        except Exception as e:
            raise BaseException(e)
        return ret

    def set_sync_server_host(self, ip, port):
        """
        Set sync server host/port
        :param ip: the server host (exp..."127.0.0.1")
        :param port: the server port (exp..."port")
        :return: raise except if error
        """
        try:
            if self.label_flag:
                self.label_plugin.set_host(ip, port)
                self.config.set_key("sync_server_host", "%s:%s" % (ip, port))
        except BaseException as e:
            raise e

    def get_sync_server_host(self):
        """
        Get sync server host,you can pull label/xpubs/tx to server
        :return: ip+port like "39.105.86.163:8080"
        """
        try:
            return self.config.get("sync_server_host", "39.105.86.163:8080")
        except BaseException as e:
            raise e

    def get_cosigner_num(self):
        try:
            self._assert_daemon_running()
            self._assert_wizard_isvalid()
        except Exception as e:
            raise BaseException(e)
        return self.wizard.get_cosigner_num()

    def _base_create_wallet(self, name, wallet_obj, coin, wallet_type):
        wallet_obj.coin = coin
        wallet_storage_path = self._wallet_path(wallet_obj.identity)
        if self.daemon.get_wallet(wallet_storage_path) is not None:
            raise BaseException(FileAlreadyExist())

        wallet_obj.ensure_storage(self._wallet_path(wallet_obj.identity))
        wallet_obj.set_name(name)
        wallet_obj.storage.set_path(wallet_storage_path)
        wallet_obj.save_db()
        self.daemon.add_wallet(wallet_obj)
        wallet_obj.update_password(old_pw=None, new_pw=None, str_pw=self.android_id, encrypt_storage=True)
        if "btc" == coin:
            wallet_obj.start_network(self.daemon.network)
        self.wallet_context.set_wallet_type(wallet_obj.identity, wallet_type)
        self.wallet = wallet_obj
        self.wallet_name = wallet_obj.basename()
        self.wizard = None
        wallet_info = CreateWalletInfo.create_wallet_info(coin_type="btc", name=self.wallet_name)
        out = self.get_create_info_by_json(wallet_info=wallet_info)
        return json.dumps(out)

    def _create_customer_wallet(self, name, xpubs, coin="btc") -> str:
        try:
            self._assert_daemon_running()
        except Exception as e:
            raise BaseException(e)

        chain_affinity = _get_chain_affinity(coin)

        if chain_affinity == "btc":
            wallet = Imported_Wallet.from_xpub(
                coin,
                self.config,
                xpubs[0][0],
                PURPOSE_TO_ADDRESS_TYPE.get(
                    int(helpers.get_path_info(self.hw_info["bip39_derivation"], PURPOSE_POS)) or "p2wpkh-p2sh"
                ),
                self.hw_info["bip39_derivation"],
                xpubs[0][1],
                hw=True,
            )
            wallet_type = "%s-hw-derived-customer-%s-%s" % ("btc", self.m, self.n)
        elif chain_affinity == "eth":
            wallet = Imported_Eth_Wallet.from_xpub(
                coin, self.config, xpubs[0][0], self.hw_info["bip39_derivation"], xpubs[0][1], hw=True
            )
            wallet_type = "%s-hw-derived-customer" % coin

        return self._base_create_wallet(name, wallet, coin, wallet_type), wallet

    def _create_multi_wallet(self, name, hd=False, hide_type=False, coin="btc", index=0):
        try:
            self._assert_daemon_running()
            self._assert_wizard_isvalid()
            temp_path = helpers.get_temp_file()
            path = self._wallet_path(temp_path)
            wallet_type = "%s-hw-%s-%s" % (coin, self.m, self.n)
            storage, db = self.wizard.create_storage(path=path, password=None, coin=coin)
        except Exception as e:
            raise BaseException(e)
        if storage:
            chain_affinity = _get_chain_affinity(coin)
            if chain_affinity == "btc":
                wallet = Wallet(db, storage, config=self.config)
                wallet.set_derived_master_xpub(self.hw_info["xpub"])
                wallet_type = "%s-hw-derived-%s-%s" % ("btc", self.m, self.n)
            elif chain_affinity == "eth":
                wallet = Standard_Eth_Wallet(db, storage, config=self.config, index=index)
                wallet_type = "%s-hw-derived" % coin
            else:
                raise UnsupportedCurrencyCoin()
            wallet.coin = coin
            wallet_storage_path = self._wallet_path(wallet.identity)
            if self.daemon.get_wallet(wallet_storage_path) is not None:
                raise BaseException(FileAlreadyExist())
            wallet.status_flag = "btc-hw-%s-%s" % (self.m, self.n)
            wallet.hide_type = hide_type
            wallet.set_name(name)
            wallet.storage.set_path(wallet_storage_path)
            wallet.save_db()
            self.daemon.add_wallet(wallet)
            wallet.update_password(old_pw=None, new_pw=None, str_pw=self.android_id, encrypt_storage=True)
            if "btc" == coin:
                wallet.start_network(self.daemon.network)

            if not hide_type:
                self.wallet_context.set_wallet_type(wallet.identity, wallet_type)
            self.wallet = wallet
            self.wallet_name = wallet.basename()
            # self.select_wallet(self.wallet_name)
            # if self.label_flag and not hide_type:
            #     wallet_name = ""
            #     if wallet_type[0:1] == "1":
            #         wallet_name = name
            #     else:
            #         wallet_name = "共管钱包"
            #     self.label_plugin.create_wallet(self.wallet, wallet_type, wallet_name)
        self.wizard = None
        wallet_info = CreateWalletInfo.create_wallet_info(coin_type="btc", name=self.wallet_name)
        out = self.get_create_info_by_json(wallet_info=wallet_info)
        return json.dumps(out), wallet

    def pull_tx_infos(self):
        """
        Get real-time multi-signature transaction info from sync_server
        """
        try:
            self._assert_wallet_isvalid()
            if self.label_flag and self.wallet.wallet_type != "standard":
                data = self.label_plugin.pull_tx(self.wallet)
                data_list = json.loads(data)
                except_list = []
                data_list.reverse()
                for txinfo in data_list:
                    try:
                        tx = tx_from_any(txinfo["tx"])
                        tx.deserialize()
                        self.do_save(tx)
                    except BaseException as e:
                        temp_data = {}
                        temp_data["tx_hash"] = txinfo["tx_hash"]
                        temp_data["error"] = str(e)
                        except_list.append(temp_data)
                        pass
                # return json.dumps(except_list)
            # self.sync_timer = threading.Timer(5.0, self.pull_tx_infos)
            # self.sync_timer.start()
        except BaseException as e:
            raise BaseException(e)

    def bulk_create_wallet(self, wallets_info):
        """
        Create wallets in bulk
        :param wallets_info:[{m,n,name,[xpub1,xpub2,xpub3]}, ....]
        :return:
        """
        wallets_list = json.loads(wallets_info)
        create_failed_into = {}
        for m, n, name, xpubs in wallets_list:
            try:
                self.import_create_hw_wallet(name, m, n, xpubs)
            except BaseException as e:
                create_failed_into[name] = str(e)
        return json.dumps(create_failed_into)

    def import_create_hw_wallet(self, name, m, n, xpubs, hide_type=False, hd=False, path="bluetooth", coin="btc"):
        """
        Create a wallet
        :param name: wallet name as string
        :param m: number of consigner as string
        :param n: number of signers as string
        :param xpubs: all xpubs as [[xpub1, device_id], [xpub2, device_id],....]
        :param hide_type: whether to create a hidden wallet as bool
        :param hd: whether to create hd wallet as bool
        :param derived: whether to create hd derived wallet as bool
        :param coin: btc/eth/bsc as string
        :return: json like {'seed':''
                            'wallet_info':''
                            'derived_info':''}
        """
        try:
            if hd:
                return self._recovery_hd_derived_wallet(xpub=self.hw_info["xpub"], hw=True, path=path)
            self.set_multi_wallet_info(name, m, n)
            xpubs_list = json.loads(xpubs)
            if self.hw_info.get("bip39_derivation"):
                path = self.hw_info.get("bip39_derivation")
                wallet_info, wallet = self._create_customer_wallet(name, xpubs_list, coin=coin)
            else:
                self.set_multi_wallet_info(name, m, n)
                derivation = self._get_hw_derivation(
                    account_id=self.hw_info["account_id"], type=self.hw_info["type"], coin=coin
                )

                for xpub_info in xpubs_list:
                    if len(xpub_info) == 2:
                        self.add_xpub(xpub_info[0], xpub_info[1], derivation)
                    else:
                        self.add_xpub(xpub_info, derivation=derivation)
                wallet_info, wallet = self._create_multi_wallet(
                    name, hd=hd, hide_type=hide_type, coin=coin, index=self.hw_info["account_id"]
                )
            customised_path_is_default_path = wallet.check_customer_and_default_path()
            derivation_path = wallet.get_derivation_path(wallet.get_addresses()[0])
            if len(self.hw_info) != 0 and customised_path_is_default_path:
                bip39_path = self.get_coin_derived_path(int(self.get_account_id(derivation_path, coin)), coin=coin)
                self.update_devired_wallet_info(bip39_path, self.hw_info["xpub"] + coin.lower(), name, coin)
            self.hw_info = {}
            return wallet_info
        except BaseException as e:
            raise e

    def get_wallet_info_from_server(self, xpub):
        """
        Get all wallet info that created by the xpub
        :param xpub: xpub from read by hardware as str
        :return:
        """
        try:
            if self.label_flag:
                Vpub_data = []
                title = "Vpub" if constants.net.TESTNET else "Zpub"
                if xpub[0:4] == title:
                    Vpub_data = json.loads(self.label_plugin.pull_xpub(xpub))
                    xpub = BIP32Node.get_p2wpkh_from_p2wsh(xpub)
                vpub_data = json.loads(self.label_plugin.pull_xpub(xpub))
                return json.dumps(Vpub_data + vpub_data if Vpub_data is not None else vpub_data)
        except BaseException as e:
            raise e

    def get_default_fee_status(self):
        """
        Get default fee,now is ETA, for btc only
        :return: The fee info as 180sat/byte when you set regtest net and the mainnet is obtained in real time
        """
        try:
            x = 1
            self.config.set_key("mempool_fees", x == 2)
            self.config.set_key("dynamic_fees", x > 0)
            return self.config.get_fee_status()
        except BaseException as e:
            raise e

    def get_amount(self, amount):
        try:
            x = Decimal(str(amount))
        except BaseException:
            return None
        # scale it to max allowed precision, make it an int
        power = pow(10, self.decimal_point)
        max_prec_amount = int(power * x)
        return max_prec_amount

    def set_dust(self, dust_flag):
        """
        Enable/disable use dust
        :param dust_flag: as bool
        :return:
        """
        if dust_flag != self.config.get("dust_flag", True):
            self.dust_flag = dust_flag
            self.config.set_key("dust_flag", self.dust_flag)

    def parse_output(self, outputs):
        all_output_add = json.loads(outputs)
        outputs_addrs = []
        for key in all_output_add:
            for address, amount in key.items():
                if amount != "!":
                    amount = self.get_amount(amount)
                    if amount <= 546:
                        raise util.DustTransaction()
                outputs_addrs.append(PartialTxOutput.from_address_and_value(address, amount))
        return outputs_addrs

    def get_coins(self, coins_info):
        coins = []
        for utxo in self.coins:
            info = utxo.to_json()
            temp_utxo = {}
            temp_utxo[info["prevout_hash"]] = info["address"]
            if coins_info.__contains__(temp_utxo):
                coins.append(utxo)
        return coins

    def format_return_data(self, feerate, size, block):
        fee = float(feerate / 1000) * size
        fiat = Decimal(fee) / COIN * price_manager.get_last_price(self.wallet.coin, self.ccy)

        fiat_str = f"{self.daemon.fx.ccy_amount_str(fiat, True)} {self.ccy}"
        ret_data = {
            "fee": self.format_amount(fee),
            "feerate": feerate / 1000,
            "time": block * BTC_BLOCK_INTERVAL_TIME,
            "fiat": fiat_str,
            "size": size,
        }
        return ret_data

    @api.api_entry()
    def get_default_fee_info(self, feerate=None, coin="btc", eth_tx_info=None):
        """
        Get default fee info for btc
        :param feerate: Custom rates need to be sapcified as true
        :param coin: btc or eth, btc default
        :param eth_tx_info: optional, dict contains one of: to_address, contract_address, value, data
        :return:
        if coin is "btc":
            if feerate is true:
                return data like {"customer":{"fee":"","feerate":, "time":"", "fiat":"", "size":""}}
            if feerate is None:
                return data like {"slow":{"fee":"","feerate":, "time":"", "fiat":"", "size":""},
                                  "normal":{"fee":"","feerate":, "time":"", "fiat":"", "size":""},
                                  "fast":{"fee":"","feerate":, "time":"", "fiat":"", "size":""},
                                  "slowest":{"fee":"","feerate":, "time":"", "fiat":"", "size":""}}
        else:
            return data like
            {"rapid": {"gas_price": 87, "time": 0.25, "gas_limit": 40000, "fee": "0.00348", "fiat": "4.77 USD"},
            "fast": {"gas_price": 86, "time": 1, "gas_limit": 40000, "fee": "0.00344", "fiat": "4.71 USD"},
            "normal": {"gas_price": 79, "time": 3, "gas_limit": 40000, "fee": "0.00316", "fiat": "4.33 USD"},
            "slow": {"gas_price": 72, "time": 10, "gas_limit": 40000, "fee": "0.00288", "fiat": "3.95 USD"}}
        """
        self._assert_wallet_isvalid()
        chain_affinity = _get_chain_affinity(coin)

        if is_coin_migrated(coin):
            if eth_tx_info:
                eth_tx_info = json.loads(eth_tx_info)
            else:
                eth_tx_info = {}
            fee_info = self.get_general_fee_info(
                to_address=eth_tx_info.pop("to_address", None),
                value=eth_tx_info.pop("value", None),
                token_address=eth_tx_info.pop("contract_address", None),
                fee_price_per_unit=eth_tx_info.pop("fee_price_per_unit", None) or eth_tx_info.pop("gas_price", None),
                fee_limit=eth_tx_info.pop("fee_limit", None) or eth_tx_info.pop("gas_limit", None),
                payload=eth_tx_info,
            )
            return json.dumps(fee_info, cls=json_encoders.DecimalEncoder)
        elif chain_affinity == "eth":
            if eth_tx_info:
                eth_tx_info = json.loads(eth_tx_info)
            else:
                eth_tx_info = {}

            eth_tx_info.pop("gas_price", None)
            eth_tx_info.pop("gas_limit", None)

            address = self.wallet.get_addresses()[0]
            fee = self.eth_estimate_fee(coin, address, **eth_tx_info)
            return json.dumps(fee, cls=json_encoders.DecimalEncoder)

        if chain_affinity != "btc":
            raise UnsupportedCurrencyCoin()

        fee_info_list = self.get_block_info()
        out_size_p2pkh = 141
        if feerate is None:
            out_info = [
                # Assuming all needed data are present
                self.format_return_data(fee_info_list.get(block), out_size_p2pkh, block)
                for block in (2, 5, 10)
            ]
        else:
            block = helpers.get_best_block_by_feerate(float(feerate) * 1000, fee_info_list)
            out_info = {"customer": self.format_return_data(float(feerate) * 1000, out_size_p2pkh, block)}
        return json.dumps(out_info)

    def eth_estimate_fee(
        self,
        coin,
        from_address,
        to_address="",
        contract_address=None,
        value="0",
        data="",
        gas_price=None,
        gas_limit=None,
    ):
        main_coin_code = chain_code = coin
        main_coin = coin_manager.get_coin_info(main_coin_code)

        if not to_address:
            gas_limit = gas_limit or 21000
        if gas_limit is None:
            # Use provider_manager.fill_unsigned_tx to estimate gas limit
            # TODO: Android app set contract_address to from_address if main
            #       coin is selected.  Remove the following
            #       "contract_address != from_address" check after it's fixed
            #       in Android codes.
            if contract_address is not None and contract_address != from_address:
                # Coin should exist, or an exception will be raised.
                coin = coin_manager.get_coin_by_token_address(chain_code, contract_address)
            else:
                coin = main_coin

            input = provider_data.TransactionInput.from_dict(
                {
                    "address": from_address,
                    "value": 0,  # This doesn't matter.
                }
            )
            output = provider_data.TransactionOutput.from_dict(
                {
                    "address": to_address,
                    "value": int(Decimal(value) * pow(10, coin.decimals)),
                    "token_address": contract_address,
                }
            )
            unsigned_tx = provider_data.UnsignedTx.from_dict(
                {
                    "inputs": [input],
                    "outputs": [output],
                    "payload": {"data": data},
                    # The following properties don't matter, just use to avoid extra client calls.
                    "fee_price_per_unit": 0,
                    "nonce": 0,
                }
            )
            try:
                unsigned_tx = provider_manager.fill_unsigned_tx(chain_code, unsigned_tx)
            except Exception:
                raise Exception(_("Estimate gas limit failed, try again."))
            gas_limit = unsigned_tx.fee_limit

        last_price = price_manager.get_last_price(main_coin_code, self.ccy)
        estimated_gas_prices = []
        for estimated_gas_price in provider_manager.get_prices_per_unit_of_fee(chain_code):
            price = math.ceil(eth_utils.from_wei(estimated_gas_price.price, "gwei"))
            fee = eth_utils.from_wei(gas_limit * eth_utils.to_wei(price, "gwei"), "ether")
            gas_price_info = {
                "gas_price": price,
                "time": Decimal(estimated_gas_price.time) / 60,
                "gas_limit": gas_limit,
                "fee": fee,
                "fiat": f"{self.daemon.fx.ccy_amount_str(Decimal(fee) * last_price, True)} {self.ccy}",
            }
            if gas_price is not None and gas_price >= price:
                return {"customer": gas_price_info}
            else:
                estimated_gas_prices.append(gas_price_info)

        return estimated_gas_prices

    def get_block_info(self):
        fee_info_list = self.config.get_block_fee_info()
        if fee_info_list is not None:
            self.config.set_key("fee_info_list", fee_info_list)
        else:
            fee_info_list = self.config.get("fee_info_list", fee_info_list)
            if fee_info_list is None:
                fee_info = read_json("server_config.json", {})
                fee_info_list = fee_info["feerate_info"]
        return fee_info_list

    @api.api_entry()
    def get_fee_by_feerate(self, coin="btc", outputs=None, message=None, feerate=None, customer=None, eth_tx_info=None):
        """
        Get fee info when Send
        :param coin: btc or eth, btc default
        :param outputs: Outputs info as json [{addr1, value}, ...]
        :param message: What you want say as sting
        :param feerate: Feerate retruned by get_default_fee_status api
        :param customer: User choose coin as bool
        :param eth_tx_info: optional, dict contains one of: to_address, contract_address, value, data, gas_price, gas_limit
        :return:
        if coin is "btc"
        json like {"amount": 0.5 BTC,
                            "size": 141,
                            "fee": 0.0003 BTC,
                            "time": 30,
                            "tx": ""}
        else if coin is "eth":
        json like {"gas_price": 110, "time": 0.25, "gas_limit": 36015, "fee": "0.00396165", "fiat": "5.43 USD"}
        """
        chain_affinity = _get_chain_affinity(coin)
        if chain_affinity == "eth":
            if eth_tx_info:
                eth_tx_info = json.loads(eth_tx_info)
            else:
                eth_tx_info = {}
            if not eth_tx_info.get("gas_price"):
                raise Exception(_("Invalid Value"))
            fee = self.eth_estimate_fee(coin, self.wallet.get_addresses()[0], **eth_tx_info)
            fee = fee.get("customer")
            return json.dumps(fee, cls=json_encoders.DecimalEncoder)

        if chain_affinity != "btc":
            raise UnsupportedCurrencyCoin()

        self._assert_wallet_isvalid()
        outputs_addrs = self.parse_output(outputs)
        if customer is None:
            coins = self.wallet.get_spendable_coins(domain=None)
        else:
            coins = self.get_coins(json.loads(customer))
        c, u, x = self.wallet.get_balance()
        if not coins and self.config.get("confirmed_only", False):
            raise BaseException(_("Please use unconfirmed utxo."))
        fee_per_kb = 1000 * Decimal(feerate)
        from functools import partial

        fee_estimator = partial(self.config.estimate_fee_for_feerate, fee_per_kb)
        # tx = self.wallet.make_unsigned_transaction(coins=coins, outputs = outputs_addrs, fee=self.get_amount(fee_estimator))
        tx = self.wallet.make_unsigned_transaction(coins=coins, outputs=outputs_addrs, fee=fee_estimator)
        tx.set_rbf(self.rbf)
        self.wallet.set_label(tx.txid(), message)
        size = tx.estimated_size()
        fee = tx.get_fee()
        self.tx = tx
        tx_details = self.wallet.get_tx_info(tx)

        fee_info_list = self.get_block_info()
        block = helpers.get_best_block_by_feerate(float(feerate) * 1000, fee_info_list)
        ret_data = {
            "amount": self.format_amount(tx_details.amount),
            "size": size,
            "fee": self.format_amount(tx_details.fee),
            "time": block * BTC_BLOCK_INTERVAL_TIME,
            "tx": str(self.tx),
        }
        return json.dumps(ret_data)

    def mktx(self, tx=None):
        """
        Confirm to create transaction, for btc only
        :param tx: tx that created by get_fee_by_feerate
        :return: json like {"tx":""}
        """

        try:
            self._assert_wallet_isvalid()
            tx = tx_from_any(tx)
            tx.deserialize()
        except Exception as e:
            raise BaseException(e)

        ret_data = {"tx": str(tx)}
        try:
            if self.label_flag and self.wallet.wallet_type != "standard":
                self.label_plugin.push_tx(self.wallet, "createtx", tx.txid(), str(self.tx))
        except Exception as e:
            log_info.info("push_tx createtx error {}.".format(e))
            pass
        json_str = json.dumps(ret_data)
        return json_str

    def deserialize(self, raw_tx):
        try:
            tx = Transaction(raw_tx)
            tx.deserialize()
        except Exception as e:
            raise BaseException(e)

    # ### coinjoin
    # def join_tx_with_another(self, tx: 'PartialTransaction', other_tx: 'PartialTransaction') -> None:
    #     if tx is None or other_tx is None:
    #         raise BaseException("tx or other_tx is empty")
    #     try:
    #         print(f"join_tx_with_another.....in.....")
    #         tx = tx_from_any(tx)
    #         other_tx = tx_from_any(other_tx)
    #         if not isinstance(tx, PartialTransaction):
    #             raise BaseException('TX must partial transactions.')
    #     except BaseException as e:
    #         raise BaseException(("Bixin was unable to parse your transaction") + ":\n" + repr(e))
    #     try:
    #         print(f"join_tx_with_another.......{tx, other_tx}")
    #         tx.join_with_other_psbt(other_tx)
    #     except BaseException as e:
    #         raise BaseException(("Error joining partial transactions") + ":\n" + repr(e))
    #     return tx.serialize_as_bytes().hex()

    # def export_for_coinjoin(self, export_tx) -> PartialTransaction:
    #     if export_tx is None:
    #         raise BaseException("export_tx is empty")
    #     export_tx = tx_from_any(export_tx)
    #     if not isinstance(export_tx, PartialTransaction):
    #         raise BaseException("Can only export partial transactions for coinjoins.")
    #     tx = copy.deepcopy(export_tx)
    #     tx.prepare_for_export_for_coinjoin()
    #     return tx.serialize_as_bytes().hex()
    # ####

    def format_amount(self, x, is_diff=False, whitespaces=False):
        return util.format_satoshis(
            x, is_diff=is_diff, num_zeros=self.num_zeros, decimal_point=self.decimal_point, whitespaces=whitespaces
        )

    def base_unit(self):
        return util.decimal_point_to_base_unit_name(self.decimal_point)

    # set use unconfirmed coin
    def set_unconf(self, x):
        """
        Enable/disable spend confirmed_only input, for btc only
        :param x: as bool
        :return:None
        """
        self.config.set_key("confirmed_only", bool(x))

    # fiat balance
    def get_currencies(self):
        """
        Get fiat list
        :return:json exp:{'CNY', 'USD'...}
        """
        self._assert_daemon_running()
        currencies = sorted(self.daemon.fx.get_currencies(self.daemon.fx.get_history_config()))
        return json.dumps(currencies)

    def get_exchanges(self):
        """
        Get exchange server list
        :return: json exp:{'exchanges', ...}
        """
        if not self.daemon.fx:
            return
        b = self.daemon.fx.is_enabled()
        if b:
            h = self.daemon.fx.get_history_config()
            c = self.daemon.fx.get_currency()
            exchanges = self.daemon.fx.get_exchanges_by_ccy(c, h)
        else:
            exchanges = self.daemon.fx.get_exchanges_by_ccy("USD", False)
        return json.dumps(sorted(exchanges))

    def set_exchange(self, exchange):
        """
        Set exchange server
        :param exchange: exchange server name as string like "exchanges"
        :return:None
        """
        if self.daemon.fx and self.daemon.fx.is_enabled() and exchange and exchange != self.daemon.fx.exchange.name():
            self.daemon.fx.set_exchange(exchange)

    def set_currency(self, ccy):
        """
        Set fiat
        :param ccy: fiat as string like "CNY"
        :return:None
        """
        self.daemon.fx.set_enabled(True)
        if ccy != self.ccy:
            self.daemon.fx.set_currency(ccy)
            self.ccy = ccy
        self.update_status()

    def get_exchange_currency(self, type, amount):
        """
        You can get coin to fiat or get fiat to coin
        :param type: base/fiat as str
        :param amount: value
        :return:
            exp:
                if you want get fiat from coin,like this:
                    get_exchange_currency("base", 1)
                return: 1,000.34 CNY

                if you want get coin from fiat, like this:
                    get_exchange_currency("fiat", 1000)
                return: 1 mBTC
        """
        text = ""
        rate = self.daemon.fx.exchange_rate() if self.daemon.fx else Decimal("NaN")
        if rate.is_nan() or amount is None:
            return text
        else:
            if type == "base":
                amount = self.get_amount(amount)
                text = self.daemon.fx.ccy_amount_str(amount * Decimal(rate) / COIN, False)
            elif type == "fiat":
                text = self.format_amount((int(Decimal(amount) / Decimal(rate) * COIN)))
            return text

    def _set_base_uint(self, base_unit):
        """
        Set base unit for(BTC/mBTC/bits/sat), for btc only
        :param base_unit: (BTC or mBTC or bits or sat) as string
        :return:None
        """
        self.base_unit = base_unit
        self.decimal_point = util.base_unit_name_to_decimal_point(self.base_unit)
        self.config.set_key("decimal_point", self.decimal_point, True)
        self.update_status()

    def format_amount_and_units(self, amount):
        try:
            self._assert_daemon_running()
        except Exception as e:
            raise BaseException(e)
        text = self.format_amount(amount) + " " + self.base_unit
        fiat = Decimal(amount) / COIN * price_manager.get_last_price(codes.BTC, self.ccy)
        fiat_str = f"{self.daemon.fx.ccy_amount_str(fiat, True)} {self.ccy}"
        text += " (%s)" % fiat_str
        return text

    # #proxy
    def set_proxy(self, proxy_mode, proxy_host, proxy_port, proxy_user, proxy_password):
        """
        Set proxy server
        :param proxy_mode: SOCK4/SOCK5 as string
        :param proxy_host: server ip
        :param proxy_port: server port
        :param proxy_user: login user that you registed
        :param proxy_password: login password that you registed
        :return: raise except if error
        """
        try:
            net_params = self.network.get_parameters()
            proxy = None
            if proxy_mode != "" and proxy_host != "" and proxy_port != "":
                proxy = {
                    "mode": str(proxy_mode).lower(),
                    "host": str(proxy_host),
                    "port": str(proxy_port),
                    "user": str(proxy_user),
                    "password": str(proxy_password),
                }
            net_params = net_params._replace(proxy=proxy)
            self.network.run_from_another_thread(self.network.set_parameters(net_params))
        except BaseException as e:
            raise e

    def recover_tx_info(self, tx):
        try:
            tx = tx_from_any(str(tx))
            temp_tx = copy.deepcopy(tx)
            temp_tx.deserialize()
            temp_tx.add_info_from_wallet(self.wallet)
            return temp_tx
        except BaseException as e:
            raise e

    def get_tx_info_from_raw(self, raw_tx, tx_list=None):
        """
        You can get detail info from a raw_tx, for btc only
        :param raw_tx: Raw tx as string
        :return: json like {'txid': ,
                            'can_broadcast': true,
                            'amount': "",
                            'fee': "",
                            'description': "",
                            'tx_status': "",
                            'sign_status': "",
                            'output_addr': "",
                            'input_addr': ["addr", ],
                            'height': 2000,
                            'cosigner': "",
                            'tx': "",
                            'show_status': [1, _("Unconfirmed")]}
        """
        try:
            tx = self.recover_tx_info(raw_tx)
        except Exception as e:
            tx = None
            raise BaseException(e)
        data = {}
        data = self.get_details_info(tx, tx_list=tx_list)
        return data

    def _get_input_info(self, tx, all_input_info=False):
        input_list = []
        local_addr = self.txdb.get_received_tx_input_info(tx.txid())
        if local_addr:
            addr_info = json.loads(local_addr[0][1])
            if (all_input_info and len(addr_info) > 1) or (not all_input_info and len(addr_info) == 1):
                return addr_info
        for txin in tx.inputs():
            input_info = {}
            addr, value = self.wallet.get_txin_address_and_value(txin)

            if not addr:
                import asyncio

                try:
                    addr, value = asyncio.run_coroutine_threadsafe(
                        self.gettransaction(txin.prevout.txid.hex(), txin.prevout.out_idx), self.network.asyncio_loop
                    ).result()
                except BaseException:
                    addr, value = "", 0

            input_info['address'] = addr
            input_info['amount'] = self.format_amount(value)
            input_list.append(input_info)
            if not all_input_info:
                break
        self.txdb.add_received_tx_input_info(tx.txid(), json.dumps(input_list))
        return input_list

    def get_fee_from_server(self, txid):
        """Retrieve a transaction. """
        url_info = read_json("server_config.json", {})
        url_list = url_info["btc_server"]
        for urlinfo in url_list:
            for key, url in urlinfo.items():
                url += txid
                try:
                    import requests

                    response = requests.get(url, timeout=2)
                    # response = await self.network.send_http_on_proxy("get", url, timeout=2)
                    response_json = response.json()
                except BaseException as e:
                    log_info.info("get fee from server error {}.".format(e))
                    pass
                    continue
                if response_json.__contains__("fee"):
                    return self.format_amount_and_units(response_json["fee"])
                elif response_json.__contains__("fees"):
                    return self.format_amount_and_units(response_json["fees"])
                else:
                    continue
        return ""

    # def get_receive_fee_by_hash(self, tx_hash):
    #     import asyncio
    #     try:
    #         fee = asyncio.run_coroutine_threadsafe(
    #             self.get_fee_from_server(tx_hash),
    #             self.network.asyncio_loop).result()
    #     except:
    #         fee = None
    #     return fee

    def get_details_info(self, tx, tx_list=None):
        try:
            self._assert_wallet_isvalid()
        except Exception as e:
            raise BaseException(e)
        tx_details = self.wallet.get_tx_info(tx)
        if "Partially signed" in tx_details.status:
            temp_s, temp_r = tx.signature_count()
            s = int(temp_s / len(tx.inputs()))
            r = len(self.wallet.get_keystores())
        elif "Unsigned" in tx_details.status:
            s = 0
            r = len(self.wallet.get_keystores())
        else:
            if self.wallet.wallet_type == "standard" or self.wallet.wallet_type == "imported":
                s = r = len(self.wallet.get_keystores())
            else:
                s, r = self.wallet.wallet_type.split("of", 1)
        in_list = self._get_input_info(tx)
        out_list = []
        for index, o in enumerate(tx.outputs()):
            address, value = o.address, o.value
            out_info = {}
            out_info["addr"] = address
            out_info["amount"] = self.format_amount_and_units(value)
            out_info["is_change"] = True if (index == len(tx.outputs()) - 1) and (len(tx.outputs()) != 1) else False
            out_list.append(out_info)

        amount_str = ""
        if tx_details.amount is None:
            amount_str = _("Transaction not related to the current wallet.")
        else:
            amount_str = self.format_amount_and_units(tx_details.amount)

        block_height = tx_details.tx_mined_status.height
        show_fee = ""
        if tx_details.fee is not None:
            show_fee = self.format_amount_and_units(tx_details.fee)
        else:
            if tx_list is None:
                show_fee_list = self.txdb.get_received_tx_fee_info(tx_details.txid)
                if len(show_fee_list) != 0:
                    show_fee = show_fee_list[0][1]
                if show_fee == "":
                    show_fee = self.get_fee_from_server(tx_details.txid)
                    if show_fee != "":
                        self.txdb.add_received_tx_fee_info(tx_details.txid, show_fee)
        if block_height == -2:
            status = {"status": provider_data.TransactionStatus.PENDING, "other_info": ""}
            can_broadcast = False
        else:
            status = tx_details.status
            if "Confirmed" in status:
                status = {"status": provider_data.TransactionStatus.CONFIRM_SUCCESS, "other_info": ""}
            elif "Unconfirmed" in status:
                status = {"status": provider_data.TransactionStatus.PENDING, "other_info": ""}
            elif "confirmations" in status:
                status = {
                    "status": provider_data.TransactionStatus.CONFIRM_SUCCESS,
                    "other_info": status.split(" ")[0],
                }
            can_broadcast = tx_details.can_broadcast

        ret_data = {
            "txid": tx_details.txid,
            "can_broadcast": can_broadcast,
            "amount": amount_str,
            "fee": show_fee,
            # 'description': self.wallet.get_label(tx_details.txid) if 44 != int(self.wallet.keystore.get_derivation_prefix().split('/')[COIN_POS].split('\'')[0]) else "",
            "description": "",
            "tx_status": status,
            "sign_status": [s, r],
            "output_addr": out_list,
            "input_addr": in_list,
            "height": 0 if block_height < 0 else block_height,
            "cosigner": [x.xpub if not isinstance(x, Imported_KeyStore) else "" for x in self.wallet.get_keystores()],
            "tx": str(tx),
            "show_status": [provider_data.TransactionStatus.PENDING, _("Unconfirmed")]
            if (block_height == 0 or (block_height < 0 and not can_broadcast))
            else [provider_data.TransactionStatus.CONFIRM_SUCCESS, _("Confirmed")]
            if block_height > 0
            else [provider_data.TransactionStatus.CONFIRM_REVERTED, _("Sending failure")],
        }
        json_data = json.dumps(ret_data)
        return json_data

    # invoices
    def delete_invoice(self, key):
        try:
            self._assert_wallet_isvalid()
            self.wallet.delete_invoice(key)
        except Exception as e:
            raise BaseException(e)

    def get_invoices(self):
        try:
            self._assert_wallet_isvalid()
            return self.wallet.get_invoices()
        except Exception as e:
            raise BaseException(e)

    def do_save(self, tx):
        try:
            if not self.wallet.add_transaction(tx):
                raise BaseException(
                    _(("Transaction cannot be saved. It conflicts with current history. tx={}").format(tx.txid()))
                )
        except BaseException as e:
            raise BaseException(e)
        else:
            self.wallet.save_db()

    def update_invoices(self, old_tx, new_tx):
        try:
            self._assert_wallet_isvalid()
            self.wallet.update_invoice(old_tx, new_tx)
        except Exception as e:
            raise BaseException(e)

    def clear_invoices(self):
        try:
            self._assert_wallet_isvalid()
            self.wallet.clear_invoices()
        except Exception as e:
            raise BaseException(e)

    def get_history_tx(self):
        try:
            self._assert_wallet_isvalid()
        except Exception as e:
            raise BaseException(e)
        history = reversed(self.wallet.get_history())
        all_data = [self.get_card(*item) for item in history]
        return all_data

    # get input address for receive tx
    async def gettransaction(self, txid, n):
        """Retrieve a transaction. """
        tx = None
        raw = await self.network.get_transaction(txid, timeout=3)
        if raw:
            tx = Transaction(raw)
        else:
            raise Exception("Unknown transaction")
        if tx.txid() != txid:
            raise Exception("Mismatching txid")
        addr = tx._outputs[n].address
        value = tx._outputs[n].value
        return addr, value

    def _get_btc_tx_list(self, start=None, end=None, search_type=None):  # noqa
        history_data = []
        try:
            history_info = self.get_history_tx()
            local_tx = self.txdb.get_tx_info(self.wallet.get_addresses()[0])
        except BaseException as e:
            raise e

        if search_type is None:
            history_data = history_info
        elif search_type == "send":
            for info in history_info:
                if info["is_mine"]:
                    history_data.append(info)
        elif search_type == "receive":
            for info in history_info:
                if not info["is_mine"]:
                    history_data.append(info)

        history_len = len(history_data)
        local_len = len(local_tx)
        all_tx_len = history_len + local_len
        if start is None or end is None:
            start = 0
            if "receive" == search_type:
                end = history_len
            else:
                end = all_tx_len
        if (search_type is None or "send" in search_type) and all_tx_len == self.old_history_len:
            return self.old_history_info[start:end]

        all_data = []
        if search_type == "receive":
            for pos, info in enumerate(history_data):
                if pos >= start and pos <= end:
                    self.get_history_show_info(info, all_data)
            return all_data
        else:
            self.old_history_len = all_tx_len
            for info in history_data:
                self.get_history_show_info(info, all_data)

            # local_tx = self.txdb.get_tx_info(self.wallet.get_addresses()[0])
            for info in local_tx:
                i = {}
                i["type"] = "history"
                data = self.get_tx_info_from_raw(info[3], tx_list=True)
                i["tx_status"] = {"status": provider_data.TransactionStatus.CONFIRM_REVERTED, "other_info": ""}
                i["date"] = util.format_time(int(info[4]))
                i["tx_hash"] = info[0]
                i["is_mine"] = True
                i["confirmations"] = 0
                data = json.loads(data)
                i["address"] = helpers.get_show_addr(data["output_addr"][0]["addr"])
                amount = data["amount"].split(" ")[0]
                if amount[0] == "-":
                    amount = amount[1:]
                fee = data["fee"].split(" ")[0]
                fiat = (
                    (Decimal(amount) + Decimal(fee)) / COIN * price_manager.get_last_price(self.wallet.coin, self.ccy)
                )
                fiat_str = f"{self.daemon.fx.ccy_amount_str(fiat, True)} {self.ccy}"
                show_amount = "%.8f" % (float(amount) + float(fee))
                show_amount = str(show_amount).rstrip("0")
                if show_amount[-1] == ".":
                    show_amount = show_amount[0:-1]
                i["amount"] = "%s %s (%s)" % (show_amount, self.base_unit, fiat_str)
                all_data.append(i)

            all_data.sort(reverse=True, key=lambda info: info["date"])
            self.old_history_info = all_data
            return all_data[start:end]

    def _get_general_coin_tx_list(
        self,
        coin: str,
        address: str,
        token_address: str = None,
        search_type: str = None,
        start: int = None,
        end: int = None,
    ):
        chain_code = self.wallet.coin
        __, transfer_coin, fee_coin = coin_manager.get_related_coins(chain_code)

        if token_address:
            token_address = provider_manager.verify_address(chain_code, token_address).normalized_address
            transfer_coin = coin_manager.get_coin_by_token_address(chain_code, token_address)

        start = start or 0
        end = end or start + 20
        items_per_page = max(end - start, 20)
        page_number = max(round(start / items_per_page), 0) + 1

        searching_address_as: Literal["sender", "receiver", "both"] = "both"
        if search_type == "send":
            searching_address_as = "sender"
        elif search_type == "receive":
            searching_address_as = "receiver"

        actions = transaction_manager.query_actions_by_address(
            chain_code=chain_code,
            coin_code=transfer_coin.code,
            address=address,
            page_number=page_number,
            items_per_page=items_per_page,
            searching_address_as=searching_address_as,
        )
        if not actions:
            return []

        try:
            best_block_number = provider_manager.get_best_block_number(chain_code)
        except Exception as e:
            log_info.exception(f"Error in get_best_block_number. chain_code: {chain_code}, error: {e}")
            best_block_number = 0

        address = provider_manager.verify_address(chain_code, address).normalized_address
        transfer_coin_price = price_manager.get_last_price(transfer_coin.code, self.ccy)
        fee_coin_price = (
            transfer_coin_price
            if fee_coin.code == transfer_coin.code
            else price_manager.get_last_price(fee_coin.code, self.ccy)
        )
        transfer_coin_decimal_divisor = pow(10, transfer_coin.decimals)
        fee_coin_decimal_divisor = pow(10, fee_coin.decimals)

        result = []
        processing_statuses = {
            transaction_data.TxActionStatus.CONFIRM_REVERTED,
            transaction_data.TxActionStatus.CONFIRM_SUCCESS,
            transaction_data.TxActionStatus.PENDING,
        }

        for action in actions:
            if action.status not in processing_statuses:
                continue

            show_address = action.to_address if action.from_address == address else action.from_address

            transfer_value = action.value / transfer_coin_decimal_divisor
            transfer_fiat = transfer_value * transfer_coin_price
            fee_value = (action.fee_used or action.fee_limit) * action.fee_price_per_unit / fee_coin_decimal_divisor
            fee_fiat = fee_value * fee_coin_price

            confirmed_date_str = format_time(action.block_time) if action.block_time is not None else _("Unknown")
            confirmed_block_number = action.block_number if action.block_number is not None else 0
            confirmed_number = max(best_block_number - confirmed_block_number, 0) if confirmed_block_number > 0 else 0

            detailed_status = {"status": action.status, "other_info": ""}
            if action.status == transaction_data.TxActionStatus.CONFIRM_REVERTED:
                show_status_desc = _("Sending failure")
            elif action.status == transaction_data.TxActionStatus.CONFIRM_SUCCESS:
                show_status_desc = _("Confirmed")
            else:
                show_status_desc = _("Unconfirmed")

            item = {
                "type": "histroy",
                "coin": transfer_coin.symbol,
                "tx_status": detailed_status,
                "show_status": [action.status, show_status_desc],
                "fee": f"{fee_value.normalize():f} {fee_coin.symbol} "
                f"({self.daemon.fx.ccy_amount_str(fee_fiat, True)} {self.ccy})",
                "date": confirmed_date_str,
                "tx_hash": action.txid,
                "is_mine": action.from_address == address,
                "height": confirmed_block_number,
                "confirmations": confirmed_number,
                "input_addr": [action.from_address],
                "output_addr": [action.to_address],
                "address": f"{show_address[:6]}...{show_address[-6:]}",
                "amount": f"{transfer_value.normalize():f} {transfer_coin.symbol} "
                f"({self.daemon.fx.ccy_amount_str(transfer_fiat, True)} {self.ccy})",
            }
            result.append(item)

        return result

    @api.api_entry()
    def get_detail_tx_info_by_hash(self, tx_hash):
        """
        Show detailed inputs and outputs
        :param tx_hash:
        :return: {
                "input_list": [{"address":"", "amount":""},...]
                "output_list": [{"address":"", "amount":""},...]
                }
        """
        self._assert_wallet_isvalid()
        tx = self.get_btc_raw_tx(tx_hash)
        try:
            in_list = self._get_input_info(tx, all_input_info=True)
        except Exception:
            in_list = []

        out_list = [{"address": output.address, "amount": self.format_amount(output.value)} for output in tx.outputs()]

        return json.dumps({"input_list": in_list, "output_list": out_list})

    @contextmanager
    def override_wallet(self, id):
        if id is not None:
            cache_wallet = self.wallet
            try:
                self.wallet = self.get_wallet_by_name(id)
                yield
            finally:
                self.wallet = cache_wallet
        else:
            yield

    @api.api_entry()
    def get_all_tx_list(self, search_type=None, coin="btc", contract_address=None, start=None, end=None, id=None):
        """
        Get the histroy list with the wallet that you select
        :param search_type: None/send/receive as str
        :param coin: btc/eth as string
        :param contract_address: contract address on eth base chains
        :param start: start position as int
        :param end: end position as int
        :return:
            exp:
                [{"type":"",
                 "tx_status":"",
                 "date":"",
                 "tx_hash":"",
                 "is_mine":"",
                 "confirmations":"",
                 "address":"",
                 "amount":""}, ...]
        """
        with self.override_wallet(id):
            chain_affinity = _get_chain_affinity(coin)
            if chain_affinity == "btc":
                ret = self._get_btc_tx_list(start=start, end=end, search_type=search_type)
            elif chain_affinity == "eth" or is_coin_migrated(coin):
                ret = self._get_general_coin_tx_list(
                    coin=self.wallet.coin,
                    address=self.wallet.get_addresses()[0],
                    token_address=contract_address,
                    search_type=search_type,
                    start=start,
                    end=end,
                )
            else:
                raise UnsupportedCurrencyCoin()

            return json.dumps(ret, cls=json_encoders.DecimalEncoder)

    def get_history_show_info(self, info, list_info):
        info["type"] = "history"
        data = self.get_tx_info(info["tx_hash"], tx_list=True)
        info["tx_status"] = json.loads(data)["tx_status"]
        info["address"] = (
            helpers.get_show_addr(json.loads(data)["output_addr"][0]["addr"])
            if info["is_mine"]
            else helpers.get_show_addr(json.loads(data)["input_addr"][0]["address"])
        )
        time = self.txdb.get_tx_time_info(info["tx_hash"])
        if len(time) != 0:
            info["date"] = util.format_time(int(time[0][1]))
        list_info.append(info)

    def get_btc_raw_tx(self, tx_hash):
        self._assert_wallet_isvalid()
        tx = self.wallet.db.get_transaction(tx_hash)
        if not tx:
            local_tx = self.txdb.get_tx_info(self.wallet.get_addresses()[0])
            for temp_tx in local_tx:
                if temp_tx[0] == tx_hash:
                    return self.get_tx_info_from_raw(temp_tx[3])
            raise Exception(_("Failed to get transaction details."))
        # tx = PartialTransaction.from_tx(tx)
        tx = copy.deepcopy(tx)
        try:
            tx.deserialize()
        except Exception as e:
            raise e
        tx.add_info_from_wallet(self.wallet)
        return tx

    def get_tx_info(self, tx_hash, coin="btc", tx_list=None):
        """
        Get detail info by tx_hash
        :param tx_hash: tx_hash as string
        :param tx_list: Not for app
        :param coin: btc/eth
        :return:
            Json like {'txid': ,
                    'can_broadcast': true,
                    'amount': "",
                    'fee': "",
                    'description': "",
                    'tx_status': "",
                    'sign_status': "",
                    'output_addr': "",
                    'input_addr': ["addr", ],
                    'height': 2000,
                    'cosigner': "",
                    'tx': "",
                    'show_status': [1, _("Unconfirmed")]}
        """
        self._assert_wallet_isvalid()

        chain_affinity = _get_chain_affinity(coin)
        if chain_affinity == "btc":
            tx = self.get_btc_raw_tx(tx_hash)
            return self.get_details_info(tx, tx_list=tx_list)
        elif chain_affinity == "eth" or is_coin_migrated(coin):
            return self.get_general_tx_info(tx_hash)
        else:
            raise UnsupportedCurrencyCoin()

    def get_general_tx_info(self, tx_hash) -> str:
        chain_code = self.wallet.coin
        __, transfer_coin, fee_coin = coin_manager.get_related_coins(chain_code)

        fee_coin_price = price_manager.get_last_price(fee_coin.code, self.ccy)
        transaction = provider_manager.get_transaction_by_txid(chain_code, tx_hash)

        if transaction.status == provider_data.TransactionStatus.CONFIRM_REVERTED:
            tx_status = {"status": provider_data.TransactionStatus.CONFIRM_REVERTED, "other_info": ""}
            show_status = [provider_data.TransactionStatus.CONFIRM_REVERTED, _("Sending failure")]
        elif transaction.status == provider_data.TransactionStatus.CONFIRM_SUCCESS:
            tx_status = {"status": provider_data.TransactionStatus.CONFIRM_SUCCESS, "other_info": ""}
            if transaction.block_header and transaction.block_header.confirmations > 0:
                tx_status["other_info"] = transaction.block_header.confirmations
            show_status = [provider_data.TransactionStatus.CONFIRM_SUCCESS, _("Confirmed")]
        else:
            tx_status = {"status": provider_data.TransactionStatus.PENDING, "other_info": ""}
            show_status = [provider_data.TransactionStatus.PENDING, _("Unconfirmed")]

        amount = Decimal(transaction.outputs[0].value) / pow(10, transfer_coin.decimals)
        fee = (
            Decimal(transaction.fee.used or transaction.fee.limit)
            * Decimal(transaction.fee.price_per_unit)
            / pow(10, fee_coin.decimals)
        )
        display_amount = (
            f"{amount.normalize():f} {transfer_coin.symbol} "
            f"({self.daemon.fx.ccy_amount_str(amount * fee_coin_price, True)} {self.ccy})"
        )
        display_fee = (
            f"{fee.normalize():f} {fee_coin.symbol} "
            f"({self.daemon.fx.ccy_amount_str(fee * fee_coin_price, True)} {self.ccy})"
        )

        ret = {
            "txid": tx_hash,
            "can_broadcast": False,
            "amount": display_amount,
            "fee": display_fee,
            "description": "",
            'tx_status': tx_status,
            "show_status": show_status,
            'sign_status': None,
            'output_addr': [transaction.outputs[0].address],
            'input_addr': [transaction.inputs[0].address],
            'height': transaction.block_header.block_number if transaction.block_header else 0,
            'cosigner': [],
            'tx': transaction.raw_tx,
        }

        return json.dumps(ret, cls=json_encoders.DecimalEncoder)

    def get_card(self, tx_hash, tx_mined_status, delta, fee, balance):
        try:
            self._assert_wallet_isvalid()
            self._assert_daemon_running()
        except Exception as e:
            raise BaseException(e)
        status, status_str = self.wallet.get_tx_status(tx_hash, tx_mined_status)
        label = self.wallet.get_label(tx_hash) if tx_hash else ""
        ri = {}
        ri["tx_hash"] = tx_hash
        ri["date"] = status_str
        ri["message"] = label
        ri["confirmations"] = tx_mined_status.conf
        if delta is not None:
            ri["is_mine"] = delta < 0
            if delta < 0:
                delta = -delta
            ri["amount"] = self.format_amount_and_units(delta)
            if self.fiat_unit:
                fx = self.daemon.fx
                fiat_value = delta / Decimal(bitcoin.COIN) * self.wallet.price_at_timestamp(tx_hash, fx.timestamp_rate)
                fiat_value = Fiat(fiat_value, fx.ccy)
                ri["quote_text"] = fiat_value.to_ui_string()
        return ri

    def get_wallet_address_show_UI(self, next=None):
        """
        Get receving address, for btc only
        :param next: if you want change address, you can set the param
        :return: json like {'qr_data':"", "addr":""}
        """
        try:
            self._assert_wallet_isvalid()
            show_addr_info = self.config.get("show_addr_info", {})
            if show_addr_info.__contains__(self.wallet.__str__()):
                self.show_addr = show_addr_info[self.wallet.__str__()]
            else:
                self.show_addr = self.wallet.get_addresses()[0]
            if next:
                addr = self.wallet.create_new_address(False)
                self.show_addr = addr
                show_addr_info[self.wallet.__str__()] = self.show_addr
                self.config.set_key("show_addr_info", show_addr_info)
        except Exception as e:
            raise BaseException(e)
        data_json = {}
        data_json["qr_data"] = self.show_addr
        data_json["addr"] = self.show_addr
        return json.dumps(data_json)

    def get_all_funded_address(self):
        """
        Get a address list of have balance, for btc only
        :return: json like [{"address":"", "balance":""},...]
        """
        try:
            self._assert_wallet_isvalid()
            all_addr = self.wallet.get_addresses()
            funded_addrs_list = []
            for addr in all_addr:
                c, u, x = self.wallet.get_addr_balance(addr)
                balance = c + u + x
                if balance == 0:
                    continue
                funded_addr = {}
                funded_addr["address"] = addr
                funded_addr["balance"] = self.format_amount_and_units(balance)
                funded_addrs_list.append(funded_addr)
            return json.dumps(funded_addrs_list)
        except Exception as e:
            raise BaseException(e)

    def get_unspend_utxos(self):
        """
        Get unspend utxos if you need
        :return:
        """
        try:
            coins = []
            for txin in self.wallet.get_utxos():
                d = txin.to_json()
                dust_sat = int(d["value_sats"])
                v = d.pop("value_sats")
                d["value"] = self.format_amount(v) + " " + self.base_unit
                if dust_sat <= 546:
                    if self.config.get("dust_flag", True):
                        continue
                coins.append(d)
                self.coins.append(txin)
            return coins
        except BaseException as e:
            raise e

    def save_tx_to_file(self, path, tx):
        """
        Save the psbt/tx to path
        :param path: path as string
        :param tx: raw tx as string
        :return: raise except if error
        """
        try:
            if tx is None:
                raise BaseException("The tx cannot be empty")
            tx = tx_from_any(tx)
            if isinstance(tx, PartialTransaction):
                tx.finalize_psbt()
            if tx.is_complete():  # network tx hex
                path += ".txn"
                with open(path, "w+") as f:
                    network_tx_hex = tx.serialize_to_network()
                    f.write(network_tx_hex + "\n")
            else:  # if partial: PSBT bytes
                assert isinstance(tx, PartialTransaction)
                path += ".psbt"
                with open(path, "wb+") as f:
                    f.write(tx.serialize_as_bytes())
        except Exception as e:
            raise BaseException(e)

    def read_tx_from_file(self, path: str, is_tx=True) -> str:
        """
        Import tx info from path
        :param is_tx: if True psbt tx else message
        :param path: path as string
        :return: serialized tx or file content
        """
        try:
            with open(path, "rb" if is_tx else "r") as f:
                file_content = f.read()
                if is_tx:
                    tx = tx_from_any(file_content)
        except (ValueError, IOError, os.error) as reason:
            raise BaseException(_("Failed to open file.{}").format(reason))
        else:
            return tx if is_tx else file_content

    def parse_address(self, data):
        data = data.strip()
        try:
            out = util.parse_URI(data)

            r = out.get("r")
            sig = out.get("sig")
            name = out.get("name")

            if r or (name and sig):
                if name and sig:
                    s = paymentrequest.serialize_request(out).SerializeToString()
                    result = paymentrequest.PaymentRequest(s)
                else:
                    result = asyncio.run_coroutine_threadsafe(
                        paymentrequest.get_payment_request(r), self.network.asyncio_loop
                    ).result()

                out = {"address": result.get_address(), "memo": result.get_memo()}
                if result.get_amount() != 0:
                    out["amount"] = result.get_amount()

            return out
        except Exception as e:
            raise Exception(e)

    @staticmethod
    def _parse_address_v2(data: str) -> dict:
        parse_result = urllib.parse.urlparse(data)
        # deal with cfx firstly
        if parse_result.scheme in (codes.CFX, "cfxtest"):
            maybe_address = f"{parse_result.scheme}:{parse_result.path}"
        else:
            maybe_address = parse_result.path
        # fails to parse query on android
        if "?" in maybe_address:
            maybe_address, query = maybe_address.split("?")
        else:
            query = parse_result.query

        if not maybe_address:
            raise InvalidAddressURI(f"Address not found. data: {data}")

        all_enabled_chains = coin_manager.get_all_chains()
        selection = []

        def _remove_testnet_prefix(chain_code: str) -> str:
            return chain_code[1:] if chain_code.startswith("t") else chain_code

        for chain in all_enabled_chains:
            try:
                validation = provider_manager.verify_address(chain.chain_code, maybe_address)
                if validation.is_valid:
                    selection.append(
                        {
                            "coin": _remove_testnet_prefix(chain.chain_code),
                            "prefix": chain.qr_code_prefix,
                            "encoding": validation.encoding,
                        }
                    )
            except Exception as e:
                log_info.exception(
                    f"Error in verify address. chain_code: {chain.chain_code}, maybe_address: {maybe_address}", e
                )

        if not selection:
            raise InvalidAddressURI(f"Address not found. data: {data}")

        result = {
            "address": maybe_address,
            "selection": selection,
        }

        selected_coin = selection[0]["coin"]
        if parse_result.scheme:
            for i in selection:
                if i["prefix"] == parse_result.scheme:
                    selected_coin = i["coin"]
                    break

        result["coin"] = selected_coin

        util.append_values_from_query(result, query)
        return result

    def parse_tx(self, data):
        # try to decode transaction
        try:
            # text = bh2u(base_decode(data, base=43))
            tx = self.recover_tx_info(data)
        except Exception as e:
            tx = None
            raise BaseException(e)

        data = self.get_details_info(tx)
        return data

    def parse_pr(self, data):
        """
        Deprecated
        """
        return self.parse_qr(data)

    def parse_qr(self, data: str) -> str:
        """
        Parse qr code which generated by address or tx, for btc only
        :param data: qr cata as str
        :return:
            if data is address qr data, return data like:
                {"type": 1, "data":"bcrt1qzm6y9j0zg9nnludkgtc0pvhet0sf76szjw7fjw"}
            if data is tx qr data, return data like:
                {"type": 2, "data":"02000000000103f9f51..."}
            if data is not address and not tx, return data like:
                {"type": 3, "data":"parse pr error"}

        """
        try:
            address_v2_result = self._parse_address_v2(data)
        except Exception as e:
            log_info.exception(f"Error in parse data as address v2. data: {data}", e)
            address_v2_result = None

        add_status_flag = False
        tx_status_flag = False
        add_data = None
        tx_data = None

        if not address_v2_result:
            try:
                add_data = self.parse_address(data)
                add_status_flag = True
            except BaseException as e:
                log_info.exception(f"Error in parse data as address. data: {data}", e)
                add_status_flag = False

            try:
                tx_data = self.parse_tx(data)
                tx_status_flag = True
            except BaseException as e:
                log_info.exception(f"Error in parse data as tx. data: {data}", e)
                tx_status_flag = False

        result = {}

        if address_v2_result:
            result["type"] = 1
            result["data"] = address_v2_result
        elif add_status_flag:
            result["type"] = 1
            result["data"] = add_data
        elif tx_status_flag:
            result["type"] = 2
            result["data"] = json.loads(tx_data)
        else:
            result["type"] = 3
            result["data"] = "parse pr error"

        return json.dumps(result, cls=json_encoders.DecimalEncoder)

    def update_local_info(self, txid, address, tx, msg):
        self.remove_local_tx(txid)
        self.txdb.add_tx_info(address=address, tx_hash=txid, psbt_tx="", raw_tx=tx, failed_info=msg)

    def broadcast_tx(self, tx: str) -> str:
        """
        Broadcast the tx, for btc only
        :param tx: tx as string
        :return: 'success'
        :raise: BaseException
        """
        trans = None
        is_electrm_broadcast_status = False
        is_blockbook_broadcast_status = False
        try:
            if isinstance(tx, str):
                trans = tx_from_any(tx)
                trans.deserialize()
            if self.network and self.network.is_connected():
                self.network.run_from_another_thread(self.network.broadcast_transaction(trans))
            else:
                self.txdb.add_tx_time_info(trans.txid())
                msg = _("Cannot broadcast transaction due to network connected exceptions")
                raise Exception(msg)
        except SerializationError:
            msg = _("Transaction formatter error")
            raise Exception(msg)
        except TxBroadcastError as e:
            msg = e.get_message_for_gui()
            raise Exception(msg)
        else:
            is_electrm_broadcast_status = True
            return "success"
        finally:
            if self.network and not constants.net.TESTNET:
                try:
                    restful = RestfulRequest("https://btc1.blkdb.cn/", timeout=5)
                    restful.get(f"/api/v2/sendtx/{tx}")
                except ResponseException as e:
                    msg = str(e)
                    pass
                else:
                    is_blockbook_broadcast_status = True

            if trans:
                self.txdb.add_tx_time_info(trans.txid())
                if (
                    not constants.net.TESTNET and not is_electrm_broadcast_status and not is_blockbook_broadcast_status
                ) or (constants.net.TESTNET and not is_electrm_broadcast_status):
                    self.update_local_info(trans.txid(), self.wallet.get_addresses()[0], tx, msg)

    def set_use_change(self, status_change):
        """
        Enable/disable change address, for btc only
        :param status_change: as bool
        :return: raise except if error
        """
        try:
            self._assert_wallet_isvalid()
        except Exception as e:
            raise BaseException(e)
        if self.wallet.use_change == status_change:
            return
        self.config.set_key("use_change", status_change, False)
        self.wallet.use_change = status_change

    def sign_message(self, address, message, path="android_usb", password=None):
        """
        Sign message, for btc only
        :param address: must bitcoin address, must in current wallet as string
        :param message: message need be signed as string
        :param path: NFC/android_usb/bluetooth as str, used by hardware
        :param password: as string
        :return: signature string
        """
        if path:
            self.trezor_manager.ensure_client(path)
        self._assert_wallet_isvalid()
        address = address.strip()
        message = message.strip()
        chain_affinity = _get_chain_affinity(self.wallet.coin)
        if chain_affinity == "eth":
            if not eth_utils.is_address(address):
                raise UnavailableEthAddr()
        elif chain_affinity == "btc":
            if not bitcoin.is_address(address):
                raise UnavailableBtcAddr()
            txin_type = self.wallet.get_txin_type(address)
            if txin_type not in ["p2pkh", "p2wpkh", "p2wpkh-p2sh"]:
                raise BaseException(_("Current wallet does not support signature message:{}".format(txin_type)))
        else:
            raise UnsupportedCurrencyCoin()
        if self.wallet.is_watching_only():
            raise BaseException(_("This is a watching-only wallet."))
        if not self.wallet.is_mine(address):
            raise BaseException(_("The address is not in the current wallet."))

        sig = self.wallet.sign_message(address, message, password)

        return text_utils.force_text(sig)

    def verify_message(self, address, message, signature, coin='btc', path="android_usb"):
        """
        Verify the message that you signed by sign_message, for btc only
        :param address: must bitcoin address as str
        :param message: message as str
        :param signature: sign info retured by sign_message api
        :return: true/false as bool
        """
        address = address.strip()
        message = message.strip().encode("utf-8")
        chain_affinity = _get_chain_affinity(coin)
        if chain_affinity == "btc":
            if not bitcoin.is_address(address):
                raise UnavailableBtcAddr()
        elif chain_affinity == "eth":
            if not eth_utils.is_address(address):
                raise UnavailableEthAddr()
        else:
            raise UnsupportedCurrencyCoin()
        try:
            self.trezor_manager.ensure_client(path)
            verified = self.wallet.verify_message(address, message, signature)
        except Exception:
            verified = False
        return verified

    def get_cur_wallet_token_address(self):
        """
        Get all token contract addresses in the current wallet
        :return:
        """
        return json.dumps(self.wallet.get_all_token_address())

    def get_customer_token_info(self, contract_address):
        """
        Add one token info
        :param contract_address:
        :return: {
                "chain_id": "",
                "decimals" : "",
                "address" : "",
                "symbol" : "",
                "name" : "",
                "logoURI": "",
                "rank": 0
        """
        self._assert_wallet_isvalid()
        chain_code = self.wallet.coin
        symbol, name, decimals = provider_manager.get_token_info_by_address(chain_code, contract_address)
        token_info = {
            "chain_id": coin_manager.get_chain_info(chain_code).chain_id,
            "decimals": decimals,
            "address": contract_address,
            "symbol": symbol,
            "name": name,
            "logoURI": "",
            "rank": 0,
        }

        return json.dumps(token_info)

    def get_all_token_info(self, coin=None):
        """
        Get all token information
        :return:
        """
        if coin is None:
            coin = self.wallet.coin if self.wallet is not None else "eth"

        chain_code = coin
        return json.dumps(list(self._load_tokens_dict(chain_code).values()))

    def _load_tokens_dict(self, chain_code: str) -> dict:
        tokens_dict = self._tokens_dict_of_chain.get(chain_code)

        if tokens_dict is None:
            tokens_dict = {
                i["address"].lower(): i for i in read_json(f"{chain_code}_token_list.json", {}).get("tokens", ())
            }
            self._tokens_dict_of_chain[chain_code] = tokens_dict

        return tokens_dict

    def _get_icon_by_token(self, coin, address=""):
        chain_code = coin
        if not address:
            coin = coin_manager.get_coin_info(chain_code, nullable=True)
        else:
            coin = coin_manager.query_coins_by_token_addresses(chain_code, [address])
            coin = coin[0] if coin else None
        return coin.icon if coin is not None else ""

    def get_all_customer_token_info(self, coin=None):
        if coin is None:
            coin = self.wallet.coin if self.wallet is not None else "eth"

        chain_code = coin

        token_dict = self._load_tokens_dict(chain_code)
        top_50_tokens = set(itertools.islice(token_dict.keys(), 50))
        db_token_coins = coin_manager.get_coins_by_chain(chain_code)
        custom_token_coins = (
            i for i in db_token_coins if i.token_address and i.token_address.lower() not in top_50_tokens
        )
        custom_token_info_list = [
            {
                "chain_id": coin_manager.get_chain_info(chain_code).chain_id,
                "decimals": i.decimals,
                "address": i.token_address.lower(),
                "symbol": i.symbol,
                "name": i.name,
                "logoURI": i.icon or "",
                "rank": 0,
            }
            for i in custom_token_coins
        ]

        return json.dumps(custom_token_info_list, cls=json_encoders.DecimalEncoder)

    def add_token(self, symbol, contract_addr, coin=None):
        """
        Add token to eth, for eth/bsc only
        :param symbol: coin symbol
        :param contract_addr: coin address
        :return: raise except if error
        """
        if not contract_addr:
            raise BaseException("Contract address cannot be empty")

        if coin is None:
            coin = self.wallet.coin if self.wallet is not None else "eth"

        chain_code = coin
        chain_info = coin_manager.get_chain_info(chain_code)
        if chain_info.chain_affinity == "eth":
            contract_addr = contract_addr.lower()

        token_info = self._load_tokens_dict(chain_code).get(contract_addr)
        if token_info is not None:
            symbol = token_info["symbol"]
            decimals = token_info["decimals"]
            name = token_info.get("name")
            icon = token_info.get("logoURI")
        else:
            symbol, name, decimals = provider_manager.get_token_info_by_address(chain_code, contract_addr)
            icon = None

        coin_manager.add_coin(chain_code, contract_addr, symbol, decimals, name, icon)
        self.wallet.add_contract_token(symbol, contract_addr)

    def delete_token(self, contract_addr):
        """
        Delete token from current wallet, for eth/bsc only
        :param contract_addr: coin address
        :return: raise except if error
        """
        if contract_addr:
            self.wallet.delete_contract_token(contract_addr)

    def get_general_fee_info(
        self,
        to_address=None,
        value=None,
        token_address=None,
        fee_price_per_unit=None,
        fee_limit=None,
        **kwargs,
    ):
        assert isinstance(self.wallet, GeneralWallet)
        ret = []
        estimated_fees = self.wallet.pre_send(
            to_address=to_address,
            value=value,
            token_address=token_address,
            fee_price_per_unit=fee_price_per_unit,
            fee_limit=fee_limit,
            payload=kwargs,
        )
        fee_coin_last_price = price_manager.get_last_price(self.wallet.fee_coin.code, self.ccy)

        for estimated_fee in estimated_fees:
            fiat_value = self.daemon.fx.ccy_amount_str(Decimal(estimated_fee["fee"]) * fee_coin_last_price, True)
            ret.append(
                {
                    "gas_price": estimated_fee["fee_price_per_unit"],
                    "time": estimated_fee["time"],
                    "gas_limit": estimated_fee["fee_limit"],
                    "fee": estimated_fee["fee"],
                    "fiat": f"{fiat_value} {self.ccy}",
                }
            )

        return ret

    def send_general_tx(
        self,
        to_address,
        value,
        password=None,
        token_address=None,
        fee_price_per_unit=None,
        fee_limit=None,
        nonce=None,
        auto_broadcast=True,
        **kwargs,
    ):
        assert isinstance(self.wallet, GeneralWallet)

        signed_tx = self.wallet.send(
            to_address,
            value,
            password,
            token_address=token_address,
            fee_price_per_unit=fee_price_per_unit,
            fee_limit=fee_limit,
            nonce=nonce,
            auto_broadcast=auto_broadcast,
            payload=kwargs,
        )
        if auto_broadcast:
            return signed_tx.txid
        else:
            return signed_tx.raw_tx

    @api.api_entry(force_version=api.Version.V2)
    def get_erc20_approve_action_field_data(self, spender_address: str, value: str) -> str:
        """
        Get the field data for erc20 api approve
        :param spender_address:
        :param value:
        :return: json like {"status": "", "info":""}

        exp:
            testcommond.get_erc20_approve_action_field_data("0x514910771af9ca656af840dff83e8264ecf986ca", "1000000000000000000000000000000000000000000")

            return data:
                {"status": 0, "info": "0x095ea7b3000000000000000000000000514910771af9ca656af840dff83e8264ecf986ca0000000000000000000000000000000000000000000000000000000000000001"}

        """
        coin = self.wallet.coin
        if coin == codes.CFX:
            from electrum_gui.common.provider.chains.cfx.sdk import cfx_address

            spender_address = cfx_address.Address.decode_hex_address(base32_address=spender_address)
        return "0x095ea7b3" + eth_abi.encode_abi(("address", "uint256"), (spender_address, int(value))).hex()

    def _get_action_result(self, coin: str, contract_address: str, data: str) -> str:
        chain_code = coin
        if coin == codes.CFX:
            client = provider_manager.get_client_by_chain(chain_code)
            out = client.cfx_call({"to": contract_address, "data": data})
        else:
            geth = provider_manager.get_client_by_chain(chain_code, instance_required=geth_client.Geth)
            out = geth.eth_call({"to": contract_address, "data": data})

        return str(int((out[2:]), base=16))

    @api.api_entry(force_version=api.Version.V2)
    def get_erc20_allowance_action_result(
        self, coin: str, contract_address: str, owner_address: str, spender_address: str
    ) -> Any:
        """
        Get the result of the allowance api
        :param coin: chain code
        :param contract_address: contract address
        :param owner_address:
        :param spender_address:
        :return:

        exp:
            testcommond.get_erc20_allowance_action_result("eth", "0x1f9840a85d5af5bf1d1762f925bdaddc4201f984", "0x514910771af9ca656af840dff83e8264ecf986ca", "0x514910771af9ca656af840dff83e8264ecf986ca")
            return data:
                {"status": 0, "info": "0"}
        """
        if coin == codes.CFX:
            from electrum_gui.common.provider.chains.cfx.sdk import cfx_address

            spender_address = cfx_address.Address.decode_hex_address(base32_address=spender_address)
            owner_address = cfx_address.Address.decode_hex_address(base32_address=owner_address)
        data = "0xdd62ed3e" + eth_abi.encode_abi(("address", "address"), (owner_address, spender_address)).hex()
        return self._get_action_result(coin, contract_address, data)

    @api.api_entry(force_version=api.Version.V2)
    def get_erc20_balanceof_action_result(self, coin: str, contract_address: str, owner_address: str) -> Any:
        """
        Get the result of the allowance api
        :param coin: chain code
        :param contract_address: contract address
        :param owner_address:
        :return:

        exp:
            testcommond.get_erc20_balanceof_action_result("eth", "0x1f9840a85d5af5bf1d1762f925bdaddc4201f984", "0x514910771af9ca656af840dff83e8264ecf986ca")
            return data:
                {"status": 0, "info": "0"}
        """
        if coin == codes.CFX:
            from electrum_gui.common.provider.chains.cfx.sdk import cfx_address

            owner_address = cfx_address.Address.decode_hex_address(base32_address=owner_address)
        data = "0x70a08231" + eth_abi.encode_abi(("address",), (owner_address,)).hex()
        return self._get_action_result(coin, contract_address, data)

    @api.api_entry(force_version=api.Version.V2)
    def get_tx_status_by_txid(self, txid: str, coin: str) -> str:
        """
        Get transaction status based on txid
        :param txid: txid as str
        :param coin: chain code
        :return:(Sending failure/Confirmed/Unconfirmed/{5} confirmations)

        exp:
            testcommond.get_tx_status_by_txid("0x41e11464913e92a978ee503b272e964ffaf38aed5bdd41338c52d4d77351d7db", "eth")
            return:
                {"status": 0, "info": {"status":0, "other_info": ""}}
        """
        chain_code = coin
        return provider_manager.get_transaction_by_txid(chain_code, txid).detailed_status

    def sign_eth_tx(
        self,
        to_addr,
        value,
        path="android_usb",
        password=None,
        contract_addr=None,
        gas_price=None,
        gas_limit=None,
        data=None,
        nonce=None,
        auto_send_tx=True,
    ):
        """
        Send for eth, for eth/bsc only
        :param to_addr: as string
        :param value: amount to send
        :param path: NFC/android_usb/bluetooth as str, used by hardware
        :param password: as string
        :param contract_addr: need if send to contranct
        :param gas_price: as string, unit is Gwei
        :param gas_limit: as string
        :param data: eth tx custom data, as hex string
        :param nonce: from address nonce
        :return: tx_hash as string
        """
        if is_coin_migrated(self.wallet.coin):
            return self.send_general_tx(
                to_address=to_addr,
                value=value,
                password=password,
                token_address=contract_addr,
                fee_price_per_unit=gas_price,
                fee_limit=gas_limit,
                auto_broadcast=auto_send_tx,
                nonce=nonce,
                data=data,
            )

        chain_code = self.wallet.coin
        if contract_addr is None:
            main_coin_code = coin_manager.get_chain_info(chain_code).fee_code
            coin = coin_manager.get_coin_info(main_coin_code)
        else:
            coin = coin_manager.get_coin_by_token_address(chain_code, contract_addr)

        from_address = self.wallet.get_addresses()[0]
        input = provider_data.TransactionInput.from_dict(
            {
                'address': from_address,
                'value': 0,  # This doesn't matter.
            }
        )
        value = int(Decimal(value) * pow(10, coin.decimals))
        output = provider_data.TransactionOutput.from_dict(
            {'address': to_addr, 'value': value, 'token_address': contract_addr}
        )
        if gas_limit is not None:
            gas_limit = int(gas_limit)
        if gas_price is not None:
            gas_price = eth_utils.to_wei(gas_price, "gwei")
        unsigned_tx = provider_data.UnsignedTx.from_dict(
            {
                'inputs': [input],
                'outputs': [output],
                'fee_limit': gas_limit,
                'fee_price_per_unit': gas_price,
                'payload': {'data': data},
            }
        )
        unsigned_tx = provider_manager.fill_unsigned_tx(chain_code, unsigned_tx)
        signed_tx_hex = None

        if isinstance(self.wallet.get_keystore(), Hardware_KeyStore) and path is not None:
            address_path = self.wallet.get_derivation_path(from_address)
            address_n = parse_path(address_path)
            self.trezor_manager.ensure_client(path)

            output = unsigned_tx.outputs[0]
            if output.token_address is not None:  # erc20 transfer
                to_address = output.token_address
                value = 0
            else:
                to_address = output.address
                value = output.value
            data = unsigned_tx.payload["data"]
            if data is not None:
                data = bytes.fromhex(eth_utils.remove_0x_prefix(data))
            signed_tx_hex = self.wallet.sign_transaction(
                address_n,
                unsigned_tx.nonce,
                unsigned_tx.fee_price_per_unit,
                unsigned_tx.fee_limit,
                to_address,
                value,
                data=data,
                chain_id=int(coin_manager.get_chain_info(chain_code).chain_id),
            )
        else:
            signer = helpers.EthSoftwareSigner(self.wallet, password)
            signed_tx = provider_manager.sign_transaction(chain_code, unsigned_tx, {from_address: signer})
            signed_tx_hex = signed_tx.raw_tx

        if signed_tx_hex is not None and auto_send_tx:
            try:
                receipt = provider_manager.broadcast_transaction(chain_code, signed_tx_hex)
            except provider_exceptions.TransactionAlreadyKnown:
                return None
            return receipt.txid

        return signed_tx_hex

    def dapp_eth_sign_tx(
        self,
        transaction: str,
        path="android_usb",
        password=None,
    ):
        transaction = json.loads(transaction)
        current_address = self.wallet.get_addresses()[0]
        coin = self.wallet.coin

        if transaction.get("from") and transaction["from"].lower() != current_address.lower():
            raise Exception(f"current wallet address is {current_address}, not {transaction['from']}")

        if not transaction.get("to"):
            raise Exception("'to' address not found")
        if coin == codes.CFX:
            gas_price = transaction.get("gasPrice")
        else:
            gas_price = eth_utils.from_wei(transaction["gasPrice"], "gwei") if transaction.get("gasPrice") else None
        signed_tx_hex = self.sign_eth_tx(
            to_addr=transaction["to"],
            value=eth_utils.from_wei(transaction["value"], "ether") if transaction.get("value") else 0,
            gas_price=gas_price,
            gas_limit=transaction.get("gas"),
            data=transaction.get("data"),
            nonce=transaction.get("nonce"),
            path=path,
            password=password,
            auto_send_tx=False,
        )
        if coin == codes.CFX:
            from electrum_gui.common.provider.chains.cfx.sdk import cfx_account

            signed_tx = cfx_account.Transaction.from_bytes(bytes.fromhex(signed_tx_hex[2:]))
        else:
            signed_tx = eth_account_account.Transaction.from_bytes(bytes.fromhex(signed_tx_hex[2:]))
        signed_tx_info = {
            "raw": signed_tx_hex,
            "tx": {
                "nonce": hex(signed_tx.nonce),
                "gasPrice": hex(signed_tx.gasPrice),
                "gas": hex(signed_tx.gas),
                "value": hex(signed_tx.value),
                "to": eth_utils.add_0x_prefix(signed_tx.to.hex()),
                "data": eth_utils.add_0x_prefix(signed_tx.data.hex()),
                "v": hex(signed_tx.v),
                "r": hex(signed_tx.r),
                "s": hex(signed_tx.s),
                "hash": signed_tx.hash().hex(),
            },
        }
        if coin == codes.CFX:
            from electrum_gui.common.provider.chains.cfx.sdk import cfx_address

            chain_code = coin_manager.legacy_coin_to_chain_code(self.wallet.coin)
            chain_id = int(coin_manager.get_chain_info(chain_code).chain_id)
            signed_tx_info["tx"].update(
                {
                    "to": cfx_address.Address.encode_hex_address(
                        cfx_address.eth_address_to_cfx(signed_tx.to.hex()), chain_id
                    )
                }
            )
            signed_tx_info["tx"].update({"storage_limit": hex(signed_tx.storage_limit)})
            signed_tx_info["tx"].update({"epoch_height": hex(signed_tx.epoch_height)})

        return json.dumps(signed_tx_info)

    def dapp_eth_send_tx(self, tx_hex: str):
        chain_code = self.wallet.coin
        try:
            receipt = provider_manager.broadcast_transaction(chain_code, tx_hex)
        except provider_exceptions.TransactionAlreadyKnown:
            return None
        return receipt.txid

    def dapp_eth_rpc_info(self):
        legacy_chain_code = codes.ETH
        if self.wallet is not None:
            legacy_chain_code = self.wallet.coin

        chain_code = legacy_chain_code
        chain_info = coin_manager.get_chain_info(chain_code)
        require(chain_info.chain_affinity in (codes.ETH, codes.CFX))
        ret = {
            "rpc": chain_info.clients[0]["url"],  # TODO: use the URL of an alive client
            "chain_id": chain_info.chain_id,
        }
        return json.dumps(ret)

    def dapp_eth_keccak(self, message: str) -> str:
        if message.startswith("0x"):
            message_bytes = bytes.fromhex(eth_utils.remove_0x_prefix(message))
        else:
            message_bytes = message.encode()

        return eth_utils.keccak(message_bytes).hex()

    def sign_tx(self, tx, path=None, password=None):
        """
        Sign one transaction, for btc only
        :param tx: tx info as str
        :param path: NFC/android_usb/bluetooth as str, used by hardware
        :param password: password as str
        :return: signed tx on success and raise except on failure
        """
        try:
            if path is not None:
                self.trezor_manager.ensure_client(path)
            self._assert_wallet_isvalid()
            tx = tx_from_any(tx)
            tx.deserialize()
            sign_tx = self.wallet.sign_transaction(tx, password)
            try:
                if self.label_flag and self.wallet.wallet_type != "standard":
                    self.label_plugin.push_tx(self.wallet, "signtx", tx.txid(), str(sign_tx))
            except BaseException as e:
                log_info.info("push_tx signtx error {}.".format(e))
                pass
            return self.get_tx_info_from_raw(sign_tx)
        except BaseException as e:
            # msg = e.__str__()
            # self.update_local_info(tx.txid(), self.wallet.get_addresses()[0], tx, msg)
            raise BaseException(e)

    def get_derived_list(self, xpub, hw=False):
        try:
            derived_info = DerivedInfo(self.config, hw)
            for derived_wallet in self.wallet_context.iter_derived_wallets(xpub):
                derived_info.update_recovery_info(derived_wallet['account_id'])

            if derived_info.recovery_num:
                derived_info.reset_list()
                return derived_info.get_list()
            else:
                return None
        except BaseException as e:
            raise e

    def show_address(self, address, path="android_usb", coin="btc") -> str:
        """
        Verify address on hardware, used by hardware
        :param address: address as str
        :param path: NFC/android_usb/bluetooth as str
        :return:1/except
        """
        try:
            self.trezor_manager.plugin.show_address(
                path=path, ui=CustomerUI(), wallet=self.wallet, address=address, coin=coin
            )
            return "1"
        except Exception as e:
            raise BaseException(e)

    def is_encrypted_with_hw_device(self):
        self.wallet.storage.is_encrypted_with_hw_device()

    def get_xpub_from_hw(
        self,
        path="android_usb",
        _type="p2wpkh",
        is_creating=True,
        account_id=None,
        coin="btc",
        from_recovery=False,
        bip39_derivation=None,
    ):
        """
        Get extended public key from hardware, used by hardware
        :param path: NFC/android_usb/bluetooth as str
        :param _type: p2wsh/p2pkh/p2wpkh-p2sh as string
        :coin: btc/eth as string
        :bip39_derivation: user defined path as string
        :return: xpub string
        """
        self.hw_info["device_id"] = self.trezor_manager.get_device_id(path, from_recovery=from_recovery)
        chain_affinity = _get_chain_affinity(coin)
        if bip39_derivation is None:
            if account_id is None:
                account_id = 0
            if chain_affinity == "btc":
                self.hw_info["type"] = _type
                if _type == "p2wsh":
                    derivation = purpose48_derivation(account_id, xtype="p2wsh")
                    self.hw_info["type"] = 48
                    # derivation = bip44_derivation(account_id, bip43_purpose=48)
                elif _type == "p2wpkh":
                    derivation = bip44_derivation(account_id, bip43_purpose=84)
                    self.hw_info["type"] = 84
                elif _type == "p2pkh":
                    derivation = bip44_derivation(account_id, bip43_purpose=44)
                    self.hw_info["type"] = 44
                elif _type == "p2wpkh-p2sh":
                    derivation = bip44_derivation(account_id, bip43_purpose=49)
                    self.hw_info["type"] = 49
                else:
                    derivation = bip44_derivation(account_id, bip43_purpose=84)
                    self.hw_info["type"] = 84
                xpub = self.trezor_manager.get_xpub(path, derivation, _type, is_creating)
            elif chain_affinity == "eth":
                self.hw_info["type"] = 44
                derivation = bip44_eth_derivation(account_id)

                derivation = util.get_keystore_path(derivation)
                xpub = self.trezor_manager.get_eth_xpub(path, derivation)
            else:
                raise UnsupportedCurrencyCoin()
        else:
            self.hw_info["bip39_derivation"] = bip39_derivation
            if chain_affinity == "btc":
                xpub = self.trezor_manager.get_xpub(path, bip39_derivation, _type, is_creating)
            elif chain_affinity == "eth":
                xpub = self.trezor_manager.get_eth_xpub(path, bip39_derivation)
            else:
                raise UnsupportedCurrencyCoin()

        return xpub

    def create_hw_derived_wallet(
        self, path="android_usb", _type="p2wpkh", is_creating=True, coin="btc", bip39_derivation=None
    ):
        """
        Create derived wallet by hardware, used by hardware
        :param path: NFC/android_usb/bluetooth as string
        :param _type: p2wsh/p2wsh/p2pkh/p2pkh-p2sh as string
        :coin: btc/eth as string
        :return: xpub as string
        """
        xpub = self.get_xpub_from_hw(path=path, _type=_type, coin=coin)
        self.hw_info["xpub"] = xpub
        if bip39_derivation is None:
            list_info = self.get_derived_list(xpub + coin.lower(), hw=True)
            if list_info is None:
                self.hw_info["account_id"] = 0
                dervied_xpub = self.get_xpub_from_hw(path=path, _type=_type, account_id=0, coin=coin)
                return dervied_xpub
            if len(list_info) == 0:
                raise BaseException(DerivedWalletLimit())
            dervied_xpub = self.get_xpub_from_hw(path=path, _type=_type, account_id=list_info[0], coin=coin)
            self.hw_info["account_id"] = list_info[0]
        else:
            purpose = PURPOSE_TO_ADDRESS_TYPE.get(int(helpers.get_path_info(bip39_derivation, PURPOSE_POS)))
            dervied_xpub = self.get_xpub_from_hw(
                path=path, _type=purpose, account_id=0, coin=coin, bip39_derivation=bip39_derivation
            )
        return dervied_xpub

    def export_keystore(self, password: str) -> str:
        """
        Export keystory from eth wallet, for eth only
        :param password: password as string
        :return:Keystore info for success/exception info for fuilure
        """
        try:
            address = self.wallet.get_addresses()[0]
            keystore = self.wallet.export_keystore(address, password=password)
            return json.dumps(keystore)
        except BaseException as e:
            raise e

    def export_privkey(self, password):
        """
        Export privkey for the first receiving address
        :param password: password as string
        :return: private as string

        .. code-block:: python
            testcommond.load_all_wallet()
            testcommond.select_wallet("BTC-1")
            priv = testcommond.export_privkey(password)
            if the wallet you select is "btc" wallet, you will get:
            'cVJo3o48E6j8xxbTQprEzaCWQJ7aL3Y59R2W1owzJnX8NBh5AXnt'

            if the wallet you select is "eth" wallet, you will get:
            '0x31271366888ccad468157770734b5ac0d98a9363d4b229767a28c44fde445f51'
        """
        try:
            address = self.wallet.get_addresses()[0]
            priv = self.wallet.export_private_key(address, password=password)
            if -1 != priv.find(":"):
                priv = priv.split(":")[1]
            return priv
        except BaseException as e:
            raise e

    def export_seed(self, password, name):
        """
        Export seed by on-chain wallet
        :param password: password by string
        :return: Mnemonic as string
        """
        try:
            wallet = self.daemon.get_wallet(self._wallet_path(name))
            if not wallet.has_seed():
                raise BaseException(NotSupportExportSeed())
            keystore = wallet.get_keystore()

            seed = keystore.get_seed(password)
            passphrase = keystore.get_passphrase(password)
            return seed + passphrase
        except BaseException as e:
            raise e

    def has_seed(self):
        """
        Check if the wallet have seed
        :return: True/False as bool
        """
        if not self.wallet.has_seed():
            raise BaseException(NotSupportExportSeed())
        return self.wallet.has_seed()

    def is_seed(self, x):
        try:
            seed_flag = False
            if keystore.is_seed(x):
                seed_flag = True
            else:
                is_checksum, is_wordlist = keystore.bip39_is_checksum_valid(x)
                if is_checksum:
                    seed_flag = True
            return seed_flag
        except BaseException as e:
            raise e

    def get_addrs_from_seed(self, seed, passphrase=""):
        """
        Get p2wpkh/p2wpkh-p2sh/p2pkh/electrum address by one seed
        :param seed: seed as str
        :param passphrase: passphrase if you need
        :return:
        """
        list_type_info = ["p2wpkh", "p2wpkh-p2sh", "p2pkh", "electrum"]
        out = {}
        for type in list_type_info:
            bip39_derivation = ""
            if type == "p2pkh":
                bip39_derivation = bip44_derivation(0, bip43_purpose=44)
            elif type == "p2wpkh":
                bip39_derivation = bip44_derivation(0, bip43_purpose=84)
            elif type == "p2wpkh-p2sh":
                bip39_derivation = bip44_derivation(0, bip43_purpose=49)

            if type == "electrum":
                from electrum.mnemonic import Mnemonic

                bip32_seed = Mnemonic.mnemonic_to_seed(seed, passphrase)
                rootnode = BIP32Node.from_rootseed(bip32_seed, xtype="standard")
                node = rootnode.subkey_at_private_derivation("m/0'/")
            else:
                bip32_seed = keystore.bip39_to_seed(seed, passphrase)
                rootnode = BIP32Node.from_rootseed(bip32_seed, xtype=("standard" if type == "p2pkh" else type))
                node = rootnode.subkey_at_private_derivation(bip39_derivation)

            from electrum import bip32

            xpub_master = bip32.xpub_from_xprv(node.to_xprv())
            node_master = BIP32Node.from_xkey(xpub_master)
            xpub = node_master.subkey_at_public_derivation((0,)).to_xpub()
            node = BIP32Node.from_xkey(xpub).subkey_at_public_derivation((0,))
            pubkey = node.eckey.get_public_key_bytes(compressed=True).hex()
            addr = bitcoin.pubkey_to_address("p2wpkh" if type == "electrum" else type, pubkey)

            temp = {}
            temp["addr"] = addr
            temp["derivation"] = bip39_derivation
            out[type] = temp
        return json.dumps(out)

    def get_wallet_by_name(self, name):
        return self.daemon.get_wallet(self._wallet_path(name))

    def get_xpub_by_name(self, name, wallet_obj):
        if self.wallet_context.is_hd(name):
            return self.get_hd_wallet_encode_seed()
        else:
            return wallet_obj.keystore.xpub

    def get_backup_info(self, name):
        """
        Get backup status
        :param name: Wallet key
        :return: True/False as bool
        """
        try:
            wallet = self.get_wallet_by_name(name)
            if wallet.is_watching_only() or isinstance(wallet.keystore, keystore.Imported_KeyStore):
                return True
            if wallet.has_seed():
                xpub = self.get_xpub_by_name(name, wallet)
                return self.wallet_context.get_backup_flag(xpub)
        except BaseException as e:
            raise e

        return True

    def delete_backup_info(self, name):
        """
        Delete one backup status in the config
        :param name: Wallet key
        :return: None
        """
        try:
            wallet = self.get_wallet_by_name(name)
            xpub = self.get_xpub_by_name(name, wallet)
            self.wallet_context.remove_backup_info(xpub)
        except BaseException as e:
            raise e

    def _chain_code_to_chain_name(self, coin):
        return f"{coin.upper()}" if "okt" != coin.lower() else "OEC"

    @api.api_entry()
    def create_hd_wallet(
        self, password, seed=None, passphrase="", purpose=84, strength=128, create_coin=json.dumps(["btc"])
    ):
        """
        Create hd wallet
        :param password: password as str
        :param seed: import create hd wallet if seed is not None
        :param purpose: 84/44/49 only for btc
        :param strength: Length of the　Mnemonic word as (128/256)
        :param create_coin: List of wallet types to be created like "["btc","eth"]"
        :return: json like {'seed':''
                            'wallet_info':''
                            'derived_info':''}
        """
        self._assert_daemon_running()
        new_seed = None
        wallet_data = []
        if seed is not None:
            is_checksum_valid, _ = keystore.bip39_is_checksum_valid(seed)
            if not is_checksum_valid:
                raise BaseException(InvalidBip39Seed())
            return self._recovery_hd_derived_wallet(password, seed, passphrase)

        if seed is None:
            seed = Mnemonic("english").generate(strength=strength)
            new_seed = seed

        create_coin_list = json.loads(create_coin)
        for coin in create_coin_list:
            chain_affinity = _get_chain_affinity(coin)
            wallet_info = None
            if is_coin_migrated(coin):
                chain_code = coin
                chain_info = coin_manager.get_chain_info(chain_code)
                last_hardened_level = bip44.BIP44Level[chain_info.bip44_last_hardened_level.upper()]
                target_level = bip44.BIP44Level[chain_info.bip44_target_level.upper()]
                wallet_info = self.create(
                    coin.upper(),
                    password,
                    seed=seed,
                    passphrase=passphrase,
                    bip39_derivation=bip44.BIP44Path(
                        44, chain_info.bip44_coin_type, 0, last_hardened_level=last_hardened_level
                    )
                    .to_target_level(target_level)
                    .to_bip44_path(),
                    hd=True,
                    coin=coin,
                )
            elif chain_affinity == "btc":
                wallet_info = self.create(
                    coin.upper(),
                    password,
                    seed=seed,
                    passphrase=passphrase,
                    bip39_derivation=bip44_derivation(0, purpose),
                    hd=True,
                )
            elif chain_affinity == "eth":
                wallet_info = self.create(
                    self._chain_code_to_chain_name(coin),
                    password,
                    seed=seed,
                    passphrase=passphrase,
                    bip39_derivation=bip44_eth_derivation(0),
                    hd=True,
                    coin=coin,
                )

            if wallet_info is not None:
                wallet_data.append(json.loads(wallet_info))

        key = self.get_hd_wallet_encode_seed(seed=seed)
        self.wallet_context.set_backup_info(key)

        out_info = []
        for info in wallet_data:
            out_info.append(info["wallet_info"][0])
        out = self.get_create_info_by_json(new_seed, out_info)
        return json.dumps(out)

    def get_wallet_num(self):
        list_info = json.loads(self.list_wallets())
        num = 0
        for info in list_info:
            for key, value in info.items():
                if -1 == value["type"].find("-hw-"):
                    num += 1
        return num

    def encode_mnemonics(self, seed):
        """
        Encoding of mnemonic words
        :param seed: mnemonics for 12/18/24
        :return: encoded data as string
        """
        return str(electrum_mnemonic.Mnemonic(lang='en').mnemonic_decode(seed))

    def decode_mnemonics(self, decoded_info):
        """
        Decode encoded data into mnemonics
        :param decoded_info: encoded data as string
        :return: mnemonic for 12/18/24
        """
        return electrum_mnemonic.Mnemonic(lang='en').mnemonic_encode(int(decoded_info))

    @api.api_entry(force_version=api.Version.V2)  # noqa
    def verify_legality(self, data, *, flag="seed", coin="btc", password=None):  # noqa
        """
        Verify legality for seed/private/public/address
        :param data: data as string
        :param flag: seed/private/public/address/keystore as string
        :param coin: btc/eth as string
        :return: raise except if failed
        """
        if flag == "seed":
            is_checksum, is_wordlist = keystore.bip39_is_checksum_valid(data)
            if not keystore.is_seed(data) and not is_checksum:
                raise exceptions.InvalidMnemonicFormat()
            return
        chain_affinity = _get_chain_affinity(coin)
        if coin and is_coin_migrated(coin):
            chain_code = coin
            chain_info = coin_manager.get_chain_info(chain_code)
            if flag == "private":
                data = eth_utils.remove_0x_prefix(data)
                try:
                    data = bytes.fromhex(data)
                    secret_manager.verify_key(chain_info.curve, prvkey=data)
                except ValueError:
                    raise exceptions.UnavailablePrivateKey()
            elif flag == "public":
                try:
                    data = bytes.fromhex(data)
                    secret_manager.verify_key(chain_info.curve, pubkey=data)
                except ValueError:
                    raise exceptions.UnavailablePublicKey()
            elif flag == "address":
                validation = provider_manager.verify_address(chain_code, data)
                if not validation.is_valid:
                    raise exceptions.IncorrectAddress()
        elif chain_affinity == "btc":
            if flag == "private":
                try:
                    ecc.ECPrivkey(bfh(data))
                except BaseException:
                    private_key = keystore.get_private_keys(data, allow_spaces_inside_key=False)
                    if private_key is None:
                        raise exceptions.UnavailablePrivateKey()
            elif flag == "address":
                try:
                    ecc.ECPubkey(bfh(data))
                except Exception:
                    if not keystore.is_xpub(data) and not bitcoin.is_address(data):
                        raise exceptions.UnavailableBtcAddr()
        elif chain_affinity == "eth":
            if flag == "private":
                try:
                    keys.PrivateKey(HexBytes(data))
                except BaseException:
                    raise exceptions.UnavailablePrivateKey()
            elif flag == "keystore":
                try:
                    eth_account_account.Account.decrypt(json.loads(data), password).hex()
                except (TypeError, KeyError, NotImplementedError):
                    raise exceptions.InvalidKeystoreFormat()
                except BaseException:
                    raise exceptions.InvalidPassword()

            elif flag == "public":
                try:
                    uncom_key = get_uncompressed_key(data)
                    keys.PublicKey(HexBytes(uncom_key[2:]))
                except BaseException:
                    raise exceptions.UnavailablePublicKey()
            elif flag == "address":
                if not eth_utils.is_address(data):
                    raise exceptions.IncorrectAddress()
        else:
            raise UnsupportedCurrencyCoin()

    def replace_watch_only_wallet(self, replace=True):
        """
        When a watch-only wallet exists locally and a non-watch-only wallet is created,
        the interface can be called to delete the watch-only wallet and keey the non-watch-only wallet
        :param replace:True/False as bool
        :return: wallet key as string
        """
        wallet = self.replace_wallet_info["wallet"]
        if replace:
            self.delete_wallet(password=self.replace_wallet_info["password"], name=wallet.identity)
            self.create_new_wallet_update(
                wallet=wallet,
                seed=self.replace_wallet_info["seed"],
                password=self.replace_wallet_info["password"],
                wallet_type=self.replace_wallet_info["wallet_type"],
                bip39_derivation=self.replace_wallet_info["bip39_derivation"],
            )
        self.replace_wallet_info = {}
        return str(wallet)

    def update_replace_info(
        self,
        wallet_obj,
        seed=None,
        password=None,
        wallet_type=None,
        bip39_derivation=None,
    ):
        self.replace_wallet_info["wallet"] = wallet_obj
        self.replace_wallet_info["seed"] = seed
        self.replace_wallet_info["password"] = password
        self.replace_wallet_info["wallet_type"] = wallet_type
        self.replace_wallet_info["bip39_derivation"] = bip39_derivation

    def create_new_wallet_update(
        self,
        wallet=None,
        seed=None,
        password=None,
        wallet_type=None,
        bip39_derivation=None,
    ):
        wallet.ensure_storage(self._wallet_path(wallet.identity))
        wallet.update_password(old_pw=None, new_pw=password, str_pw=self.android_id, encrypt_storage=True)
        wallet.save_db()
        self.daemon.add_wallet(wallet)
        if wallet.coin == "btc":
            wallet.start_network(self.daemon.network)
        self.wallet_context.set_wallet_type(wallet.identity, wallet_type)
        if bip39_derivation is not None:
            self.set_hd_wallet(wallet)
            self.update_devired_wallet_info(
                bip39_derivation,
                self.get_hd_wallet_encode_seed(
                    seed=seed, coin=wallet.coin, purpose=helpers.get_path_info(bip39_derivation, PURPOSE_POS)
                ),
                wallet.name,
                wallet.coin,
            )

    def _get_derived_account_id(self, xpub, hw=False):
        list_info = self.get_derived_list(xpub, hw=hw)
        if list_info is not None:
            if not list_info:
                raise BaseException(DerivedWalletLimit())
            account_id = list_info[0]
        else:
            account_id = 0
        return account_id

    @api.api_entry()
    def get_default_show_path(self, hw=False, coin=None, derived=False, purpose="49", path="android_usb"):
        """
        Get the default path for creating a wallet
        :param hw: if true, it means that you currently need to create a hardware wallet
        :param coin: btc/eth/bsc/heco as str
        :param derived: if true, it means that you currently need to create a derived wallet
        :param purpose: 44/49/84, only btc needs
        :param path: NFC/android_usb/bluetooth as str
        :return:
        """
        if not hw and is_coin_migrated(coin):
            chain_code = coin
            chain_info = coin_manager.get_chain_info(chain_code)
            address_encoding = None
            if purpose:
                purpose_to_address_encoding = {v: k for k, v in chain_info.bip44_purpose_options.items()}
                address_encoding = purpose_to_address_encoding.get(int(purpose))

            address_encoding = address_encoding or chain_info.default_address_encoding
            if derived:
                return wallet_manager.generate_next_bip44_path_for_derived_primary_wallet(
                    chain_code, address_encoding
                ).to_bip44_path()
            else:
                return wallet_manager.get_default_bip44_path(chain_code, address_encoding).to_bip44_path()

        account_id = 0
        if not hw and derived:
            derive_key = self.get_hd_wallet_encode_seed(coin=coin, purpose=str(purpose))
            account_id = self._get_derived_account_id(derive_key)
        elif hw:
            xpub = self.get_xpub_from_hw(path=path, coin=coin, _type=PURPOSE_TO_ADDRESS_TYPE.get(purpose, "p2wpkh"))
            account_id = self._get_derived_account_id(xpub + coin.lower(), hw=hw)

        chain_affinity = _get_chain_affinity(coin)
        if chain_affinity == 'btc':
            return "%s/0/0" % keystore.bip44_derivation(account_id, bip43_purpose=int(purpose))
        elif chain_affinity == "eth":
            return keystore.bip44_eth_derivation(account_id)
        else:
            raise UnsupportedCurrencyCoin()

    def create_and_check_wallet(  # noqa
        self,
        name,
        password=None,
        seed=None,
        passphrase="",
        bip39_derivation=None,
        master=None,
        addresses=None,
        privkeys=None,
        purpose=49,
        coin="btc",
        keystores=None,
        keystore_password=None,
        strength=128,
        is_customized_path=False,
    ):

        wallet = None
        watch_only = False
        new_seed = False
        chain_affinity = _get_chain_affinity(coin)

        if addresses is not None:
            watch_only = True
            wallet_type = f"{coin}-watch-standard"
            if is_coin_migrated(coin):
                wallet = GeneralWallet.from_pubkey_or_addresses(name, coin, self.config, addresses)
            elif chain_affinity == "btc":
                if keystore.is_xpub(addresses):
                    wallet = Standard_Wallet.from_master_key("btc", self.config, addresses)
                else:
                    wallet = Imported_Wallet.from_pubkey_or_addresses(coin, self.config, addresses)
            elif chain_affinity == "eth":
                wallet = Imported_Eth_Wallet.from_pubkey_or_addresses(coin, self.config, addresses)
            else:
                raise UnsupportedCurrencyCoin()
        elif is_coin_migrated(coin) and (privkeys is not None or keystores is not None):
            wallet_type = f"{coin}-private-standard"
            if keystores is not None:
                wallet = GeneralWallet.from_keystore(name, coin, self.config, keystores, keystore_password, password)
            else:
                wallet = GeneralWallet.from_prvkeys(name, coin, self.config, privkeys, password)
        elif privkeys is not None and chain_affinity == "btc":
            wallet_type = f"{coin}-private-standard"
            wallet = Imported_Wallet.from_privkeys(coin, self.config, privkeys, purpose)
        elif chain_affinity == "eth" and (privkeys is not None or keystores is not None):
            wallet_type = f"{coin}-private-standard"
            if keystores is not None:
                # keystores always has higher priority
                wallet = Imported_Eth_Wallet.from_keystores(coin, self.config, keystores, keystore_password)
            else:
                wallet = Imported_Eth_Wallet.from_privkeys(coin, self.config, privkeys)
        elif bip39_derivation is not None and seed is not None and not is_customized_path:
            wallet_type = f"{coin}-derived-standard"
            if is_coin_migrated(coin):
                wallet = GeneralWallet.from_seed_or_bip39(
                    name,
                    coin,
                    self.config,
                    seed,
                    password=password,
                    passphrase=passphrase,
                    bip44_path=bip39_derivation,
                    as_primary_wallet=True,
                )
            elif chain_affinity == "btc":
                wallet = Standard_Wallet.from_seed_or_bip39(coin, self.config, seed, passphrase, bip39_derivation)
            elif chain_affinity == "eth":
                derivation = util.get_keystore_path(bip39_derivation)
                index = int(helpers.get_path_info(bip39_derivation, INDEX_POS))
                wallet = Standard_Eth_Wallet.from_seed_or_bip39(coin, index, self.config, seed, passphrase, derivation)
        elif bip39_derivation is not None and is_customized_path:
            if seed is None:
                seed = Mnemonic("english").generate(strength=strength)
                new_seed = True
            wallet_type = f"{coin}-customer-standard"
            if is_coin_migrated(coin):
                wallet = GeneralWallet.from_seed_or_bip39(
                    name,
                    coin,
                    self.config,
                    seed,
                    password=password,
                    passphrase=passphrase,
                    bip44_path=bip39_derivation,
                    as_primary_wallet=False,
                )
            elif chain_affinity == "btc":
                wallet = Imported_Wallet.from_seed(
                    coin,
                    self.config,
                    seed,
                    passphrase,
                    PURPOSE_TO_ADDRESS_TYPE.get(
                        int(helpers.get_path_info(bip39_derivation, PURPOSE_POS)) or "p2wpkh-p2sh"
                    ),
                    bip39_derivation,
                )
            elif chain_affinity == "eth":
                wallet = Imported_Eth_Wallet.from_seed(coin, self.config, seed, passphrase, bip39_derivation)
        elif master is not None:
            # TODO: master is only for btc?
            wallet_type = "btc-standard"
            wallet = Standard_Wallet.from_master_key("btc", self.config, master)
        else:
            wallet_type = f"{coin}-standard"
            if seed is None:
                seed = Mnemonic("english").generate(strength=strength)
                new_seed = True

            if is_coin_migrated(coin):
                wallet = GeneralWallet.from_seed_or_bip39(
                    name,
                    coin,
                    self.config,
                    seed,
                    password=password,
                    passphrase=passphrase,
                    as_primary_wallet=False,
                )
            elif chain_affinity == "btc":
                wallet = Standard_Wallet.from_seed_or_bip39(
                    coin, self.config, seed, passphrase, bip44_derivation(0, purpose)
                )
            elif chain_affinity == "eth":
                derivation = util.get_keystore_path(bip44_eth_derivation(0))
                index = 0
                wallet = Standard_Eth_Wallet.from_seed_or_bip39(coin, index, self.config, seed, passphrase, derivation)

        wallet.set_name(name)
        exist_wallet = self.daemon.get_wallet(self._wallet_path(wallet.identity))
        if exist_wallet is not None:
            if not watch_only and exist_wallet.is_watching_only():
                self.update_replace_info(
                    wallet,
                    seed=seed,
                    password=password,
                    wallet_type=wallet_type,
                    bip39_derivation=bip39_derivation,
                )
                raise BaseException("Replace Watch-olny wallet:%s" % wallet.identity)
            else:
                raise BaseException(FileAlreadyExist())
        return wallet, wallet_type, new_seed

    @orm_database.db.atomic()
    def create(  # noqa
        self,
        name,
        password=None,
        seed_type="segwit",
        seed=None,
        passphrase="",
        bip39_derivation=None,
        master=None,
        addresses=None,
        privkeys=None,
        hd=False,
        purpose=49,
        coin="btc",
        keystores=None,
        keystore_password=None,
        strength=128,
        is_customized_path=False,
    ):
        """
        Create or restore a new wallet
        :param name: Wallet name as string
        :param password: Password ans string
        :param seed_type: Not for now
        :param seed: Mnemonic word as string
        :param passphrase:Customised passwords as string
        :param bip39_derivation:Not for now
        :param master:Not for now
        :param addresses:To create a watch-only wallet you need
        :param privkeys:To create a wallet with a private key you need
        :param hd:Not for app
        :param purpose:BTC address type as (44/49/84), for BTC only
        :param coin:"btc"/"eth" as string to specify whether to create a BTC/ETH wallet
        :param keystores:as string for ETH only
        :param strength:Length of the　Mnemonic word as (128/256)
        :param is_customized_path: Set to true when the user-defined path is modified
        :return: json like {'seed':''
                            'wallet_info':''
                            'derived_info':''}
        .. code-block:: python
                create a btc wallet by address:
                    create("test5", addresses="bcrt1qzm6y9j0zg9nnludkgtc0pvhet0sf76szjw7fjw")
                create a eth wallet by address:
                    create("test4", addresses="0x....", coin="eth")

                create a btc wallet by privkey:
                    create("test3", password=password, purpose=84, privkeys="cRR5YkkGHTph8RsM1bQv7YSzY27hxBBhoJnVdHjGnuKntY7RgoGw")
                create a eth wallet by privkey:
                    create("test3", password=password, privkeys="0xe6841ceb170becade0a4aa3e157f08871192f9de1c35835de5e1b47fc167d27e", coin="eth")

                create a btc wallet by seed:
                    create(name, password, seed='pottery curtain belt canal cart include raise receive sponsor vote embody offer')
                create a eth wallet by seed:
                    create(name, password, seed='pottery curtain belt canal cart include raise receive sponsor vote embody offer', coin="eth")

        """
        try:
            if self.get_wallet_num() == 0:
                self.check_pw_wallet = None
            if addresses is None:
                self.check_password(password)
        except BaseException as e:
            raise e

        # wallet = None
        # watch_only = False
        # new_seed = False
        wallet, wallet_type, new_seed = self.create_and_check_wallet(
            name,
            password=password,
            seed=seed,
            passphrase=passphrase,
            bip39_derivation=bip39_derivation,
            master=master,
            addresses=addresses,
            privkeys=privkeys,
            purpose=purpose,
            coin=coin,
            keystores=keystores,
            keystore_password=keystore_password,
            is_customized_path=is_customized_path,
        )

        self.create_new_wallet_update(
            wallet=wallet, seed=seed, password=password, wallet_type=wallet_type, bip39_derivation=bip39_derivation
        )
        if new_seed:
            self.wallet_context.set_backup_info(wallet.keystore.xpub)
        ret = {
            "seed": seed if new_seed else "",
            "wallet_info": [{"coin_type": coin, "name": wallet.identity, "exist": 0}],
            "derived_info": [],
        }
        return json.dumps(ret)

    def get_create_info_by_json(self, seed="", wallet_info=None, derived_info=None):
        from electrum_gui.android.create_wallet_info import CreateWalletInfo

        create_wallet_info = CreateWalletInfo()
        create_wallet_info.add_seed(seed)
        create_wallet_info.add_wallet_info(wallet_info)
        create_wallet_info.add_derived_info(derived_info)
        return create_wallet_info.to_json()

    def is_watch_only(self):
        """
        Check if it is watch only wallet
        :return: True/False as bool
        """
        self._assert_wallet_isvalid()
        return self.wallet.is_watching_only()

    def _load_all_wallet(self):
        """
        Load all wallet info
        :return:None
        """
        name_wallets = sorted([name for name in os.listdir(self._wallet_path())])
        for name in name_wallets:
            try:
                self.load_wallet(name, password=self.android_id)
            except InvalidPassword:
                storage_password = "112233%s" % self.android_id[-8:]
                wallet = self.load_wallet(name, password=storage_password)
                wallet.force_change_storage_password(self.android_id)

    @api.api_entry()
    def update_wallet_password(self, old_password, new_password):
        """
        Update password
        :param old_password: old_password as string
        :param new_password: new_password as string
        :return:None
        """
        self._assert_daemon_running()
        for _name, wallet in self.daemon._wallets.items():
            wallet.update_password(old_pw=old_password, new_pw=new_password, str_pw=self.android_id)

    @api.api_entry()
    def check_password(self, password):
        """
        Check wallet password
        :param password: as string
        :return: raise except if error
        """
        if self.check_pw_wallet is None:
            self.check_pw_wallet = self.get_check_wallet()
        if self.check_pw_wallet is not None:
            self.check_pw_wallet.check_password(password, str_pw=self.android_id)

    def recovery_confirmed(self, name_list, hw=False):
        """
        If you import hd wallet by seed, you will get a name list
        and you need confirm which wallets you want to import
        :param name_list: wallets you want to import as list like [name, name2,...]
        :param hw: True if you recovery from hardware
        :return:None
        """
        name_list = json.loads(name_list)
        if len(name_list) != 0:
            for name in name_list:
                if name not in self.recovery_wallets:
                    continue
                recovery_info = self.recovery_wallets[name]
                wallet = recovery_info["wallet"]
                self.daemon.add_wallet(wallet)
                wallet.hide_type = False
                wallet.save_db()
                if not hw:
                    self.set_hd_wallet(wallet)
                coin = wallet.coin
                chain_affinity = _get_chain_affinity(coin)
                if chain_affinity == "eth":
                    wallet_type = "%s-hw-derived-%s-%s" % (coin, 1, 1) if hw else ("%s-derived-standard" % coin)
                    self.update_devired_wallet_info(
                        bip44_eth_derivation(recovery_info["account_id"]),
                        recovery_info["key"],
                        wallet.get_name(),
                        coin,
                    )
                elif chain_affinity == "btc":
                    wallet_type = "btc-hw-derived-%s-%s" % (1, 1) if hw else ("btc-derived-standard")
                    self.update_devired_wallet_info(
                        bip44_derivation(recovery_info["account_id"], bip43_purpose=84),
                        recovery_info["key"],
                        wallet.get_name(),
                        coin,
                    )
                else:
                    raise UnsupportedCurrencyCoin()
                self.wallet_context.set_wallet_type(wallet.identity, wallet_type)
                self.recovery_wallets.pop(name)
        for name, info in self.recovery_wallets.items():
            info["wallet"].stop()
        self.recovery_wallets.clear()

    def delete_derived_wallet(self):
        wallets = json.loads(self.list_wallets())
        for wallet_info in wallets:
            for wallet_id, info in wallet_info.items():
                try:
                    if -1 != info["type"].find("-derived-") and -1 == info["type"].find("-hw-"):
                        key_in_daemon = self._wallet_path(wallet_id)
                        wallet_obj = self.daemon.get_wallet(key_in_daemon)
                        self.delete_wallet_derived_info(wallet_obj)
                        self.delete_wallet_from_deamon(key_in_daemon)
                        self.wallet_context.remove_type_info(wallet_id)
                except Exception as e:
                    raise BaseException(e)
        self.hd_wallet = None

    def get_check_wallet(self):
        wallets = self.daemon.get_wallets()
        for key, wallet in wallets.items():
            if not isinstance(wallet.keystore, Hardware_KeyStore) and not wallet.is_watching_only():
                return wallet
                # key = sorted(wallets.keys())[0]
                # # value = wallets.values()[0]
                # return wallets[key]

    def filter_wallet(self):
        recovery_list = []
        wallet_info = [item["wallet"] for item in self.recovery_wallets.values()]
        wallet_info = sorted(wallet_info, key=attrgetter('coin'))
        for coin, items in itertools.groupby(wallet_info, key=attrgetter('coin')):
            chain_affinity = _get_chain_affinity(coin)
            if chain_affinity == "eth":
                wallets = list(items)
                chain_code = coin
                try:
                    address_info_list = provider_manager.batch_get_address(
                        chain_code, [wallet.get_addresses()[0] for wallet in wallets]
                    )
                except Exception:
                    address_info_list = None

                if address_info_list is None:
                    continue

                fee_code = coin_manager.get_chain_info(chain_code).fee_code
                main_coin_price = price_manager.get_last_price(fee_code, self.ccy)
                for index, address_info in enumerate(address_info_list):
                    if not address_info.existing:
                        continue
                    main_coin_balance = Decimal(eth_utils.from_wei(address_info.balance, "ether"))
                    fiat_str = self.daemon.fx.ccy_amount_str(main_coin_balance * main_coin_price, True)
                    balance = f"{main_coin_balance} ({fiat_str} {self.ccy})"
                    recovery_list.append(
                        {
                            "coin": coin,
                            "blance": balance,
                            "name": str(wallets[index]),
                            "label": wallets[index].get_name(),
                            "exist": "1"
                            if self.daemon.get_wallet(self._wallet_path(wallets[index].identity)) is not None
                            else "0",
                        }
                    )
            elif chain_affinity == "btc":
                for wallet in items:
                    if not bool(wallet.get_history()):
                        continue
                    c, u, _ = wallet.get_balance()
                    balance = self.format_amount_and_units(c + u)
                    recovery_list.append(
                        {
                            "coin": coin,
                            "blance": balance,
                            "name": str(wallet),
                            "label": wallet.get_name(),
                            "exist": "1"
                            if self.daemon.get_wallet(self._wallet_path(wallet.identity)) is not None
                            else "0",
                        }
                    )
            else:
                raise UnsupportedCurrencyCoin()
        return recovery_list

    def update_recovery_wallet(self, key, wallet_obj, bip39_derivation, name, coin):
        wallet_info = {}
        wallet_info["key"] = key
        wallet_info["wallet"] = wallet_obj
        wallet_info["account_id"] = int(self.get_account_id(bip39_derivation, coin))
        wallet_info["name"] = name
        return wallet_info

    def get_coin_derived_path(self, account_id, coin="btc", purpose=84):
        chain_affinity = _get_chain_affinity(coin)
        if chain_affinity == "btc":
            return bip44_derivation(account_id, bip43_purpose=purpose)
        elif chain_affinity == "eth":
            return bip44_eth_derivation(account_id)
        else:
            raise UnsupportedCurrencyCoin()

    def recovery_import_create_hw_wallet(self, i, name, m, n, xpubs, coin="btc"):
        try:
            self.set_multi_wallet_info(name, m, n)
            derivation = self._get_hw_derivation(account_id=i, type=self.hw_info["type"], coin=coin)
            self.add_xpub(xpubs, self.hw_info["device_id"], derivation)

            temp_path = helpers.get_temp_file()
            path = self._wallet_path(temp_path)
            storage, db = self.wizard.create_storage(path=path, password="", coin=coin)
            if storage:
                chain_affinity = _get_chain_affinity(coin)
                if chain_affinity == "eth":
                    wallet = Standard_Eth_Wallet(db, storage, config=self.config, index=i)
                elif chain_affinity == "btc":
                    wallet = Wallet(db, storage, config=self.config)
                else:
                    raise UnsupportedCurrencyCoin()
                wallet.set_name(name)
                wallet.coin = coin
                wallet.hide_type = True
                wallet.storage.set_path(self._wallet_path(wallet.identity))
                wallet.update_password(old_pw=None, new_pw=None, str_pw=self.android_id, encrypt_storage=True)
                if coin == "btc":
                    wallet.start_network(self.daemon.network)
                self.recovery_wallets[wallet.identity] = self.update_recovery_wallet(
                    xpubs + coin.lower(), wallet, self.get_coin_derived_path(i, coin), name, coin
                )
                self.wizard = None
        except Exception as e:
            raise BaseException(e)

    def get_hd_wallet(self):
        if self.hd_wallet is None:
            wallets = self.daemon.get_wallets()
            for key, wallet in wallets.items():
                if self.wallet_context.is_hd(wallet.identity):
                    self.hd_wallet = wallet
                    break
            else:
                raise BaseException(UnavaiableHdWallet())

        return self.hd_wallet

    def get_hd_wallet_encode_seed(self, seed=None, coin="", passphrase="", purpose=""):
        if seed is not None:
            path = bip44_derivation(0, 84)
            ks = keystore.from_bip39_seed(seed, passphrase, path)
            self.config.set_key("current_hd_xpub", ks.xpub)
            return ks.xpub + coin.lower() + purpose
        else:
            return self.config.get("current_hd_xpub", "") + coin.lower() + purpose

    @classmethod
    def _set_recovery_flag(cls, flag=False):
        """
        Support recovery process was withdrawn
        :param flag: true/false
        :return:
        """
        cls._recovery_flag = flag

    def filter_wallet_with_account_is_zero(self):
        wallet_list = []
        for wallet_id, wallet_info in self.recovery_wallets.items():
            try:
                wallet = wallet_info["wallet"]
                coin = wallet.coin
                derivation = wallet.get_derivation_path(wallet.get_addresses()[0])
                account_id = int(self.get_account_id(derivation, coin))
                purpose = int(derivation.split("/")[PURPOSE_POS].split("'")[0])
                if account_id != 0:
                    continue
                if purpose == 49 or _get_chain_affinity(coin) == "eth":
                    exist = 1 if self.daemon.get_wallet(self._wallet_path(wallet_id)) is not None else 0
                    wallet_info = {
                        'coin_type': coin,
                        'name': wallet.identity,
                        'exist': exist,
                        'label': wallet.get_name(),
                    }
                    wallet_list.append(wallet_info)
            except BaseException as e:
                raise e
        return wallet_list

    def _recovery_hd_derived_wallet(
        self, password=None, seed=None, passphrase="", xpub=None, hw=False, path="bluetooth"
    ):
        eths_xpub = None
        self.recovery_wallets = {}

        def recovery_wallet(self, purpose: int, coin: str = "btc") -> None:
            def recovery_create_subfun(self, coin, account_id, name) -> None:
                nonlocal eths_xpub
                try:
                    chain_affinity = _get_chain_affinity(coin)
                    if chain_affinity == "btc":
                        pass
                    elif chain_affinity == "eth":
                        if hw and eths_xpub:
                            self.recovery_import_create_hw_wallet(account_id, name, 1, 1, eths_xpub, coin=coin)
                            return
                    else:
                        raise UnsupportedCurrencyCoin()

                    if not AndroidCommands._recovery_flag:
                        self.recovery_wallets.clear()
                        AndroidCommands._set_recovery_flag(True)
                        raise UserCancel()
                    if hw:
                        _type = PURPOSE_TO_ADDRESS_TYPE.get(purpose, "")
                        xpub = self.get_xpub_from_hw(
                            path=path, _type=_type, account_id=account_id, coin=coin, from_recovery=True
                        )
                        if coin != "btc":
                            eths_xpub = xpub
                        self.recovery_import_create_hw_wallet(account_id, name, 1, 1, xpub, coin=coin)
                    else:
                        bip39_derivation = self.get_coin_derived_path(account_id, coin, purpose=purpose)
                        self.recovery_create(
                            name,
                            seed,
                            password=password,
                            bip39_derivation=bip39_derivation,
                            passphrase=passphrase,
                            coin=coin,
                        )
                except BaseException as e:
                    raise e

            derived = DerivedInfo(self.config)
            AndroidCommands._set_recovery_flag(True)
            for derived_wallet in self.wallet_context.iter_derived_wallets(xpub):
                recovery_create_subfun(self, coin, derived_wallet['account_id'], derived_wallet['name'])
                derived.update_recovery_info(derived_wallet['account_id'])
            derived.reset_list()
            account_list = derived.get_list()
            for i in account_list:
                recovery_create_subfun(self, coin, i, f"{coin}-derived-{purpose}-{i}")

        if hw:
            for add_type in [49, 44, 84]:
                recovery_wallet(self, add_type)

            for chain_code in chains_config.get_evm_chains_to_recover():
                recovery_wallet(self, 44, coin=chain_code)
        else:
            for add_type in ["49", "44", "84"]:
                xpub = self.get_hd_wallet_encode_seed(seed=seed, coin="btc", purpose=add_type)
                recovery_wallet(self, int(add_type))

            for chain_code in chains_config.get_evm_chains_to_recover():
                xpub = self.get_hd_wallet_encode_seed(seed=seed, coin=chain_code, purpose="44")
                recovery_wallet(self, 44, coin=chain_code)

        recovery_list = self.filter_wallet()
        wallet_data = self.filter_wallet_with_account_is_zero()
        out = self.get_create_info_by_json(wallet_info=wallet_data, derived_info=recovery_list)
        return json.dumps(out)

    def create_derived_wallet(self, name, password, coin="btc", purpose=84, strength=128):
        """
        Create BTC/ETH derived wallet
        :param name: name as str
        :param password: password as str
        :param coin: btc/eth as str
        :param purpose: (44/84/49) for btc only
        :param strength: Length of the　Mnemonic word as (128/256)
        :return: json like {'seed':''
                            'wallet_info':''
                            'derived_info':''}
        """
        # NOTE: change the below check if adding support for other coins.
        # This should be a call like this:
        # ```
        # if coin_manager.is_derived_wallet_supported(coin):
        #     raise BaseException(f"Derived wallet of {coin} isn't supported.")
        # ```
        try:
            self.check_password(password)
        except BaseException as e:
            raise e

        seed = self.get_hd_wallet().get_seed(password)
        chain_affinity = _get_chain_affinity(coin)
        if is_coin_migrated(coin):
            chain_code = coin
            chain_info = coin_manager.get_chain_info(chain_code)
            address_encoding = None
            if purpose:
                purpose_to_address_encoding = {v: k for k, v in chain_info.bip44_purpose_options.items()}
                address_encoding = purpose_to_address_encoding.get(int(purpose))

            derived_path = wallet_manager.generate_next_bip44_path_for_derived_primary_wallet(
                chain_code, address_encoding
            ).to_bip44_path()
        elif chain_affinity == "btc":
            encode_seed = self.get_hd_wallet_encode_seed(seed=seed, coin=coin, purpose=str(purpose))
            account_id = self._get_derived_account_id(encode_seed)
            derived_path = bip44_derivation(account_id, int(purpose))
        elif chain_affinity == "eth":
            encode_seed = self.get_hd_wallet_encode_seed(seed=seed, coin=coin, purpose="44")
            account_id = self._get_derived_account_id(encode_seed)
            derived_path = bip44_eth_derivation(account_id)
        else:
            raise UnsupportedCurrencyCoin()

        return self.create(name, password, seed=seed, bip39_derivation=derived_path, strength=strength, coin=coin)

    def recovery_create(self, name, seed, password, bip39_derivation, passphrase="", coin="btc"):
        try:
            self.check_password(password)
        except BaseException as e:
            raise e
        account_id = int(self.get_account_id(bip39_derivation, coin))
        purpose = int(helpers.get_path_info(bip39_derivation, PURPOSE_POS))
        chain_affinity = _get_chain_affinity(coin)

        if account_id == 0:
            if purpose == 49:
                name = f"{coin.upper()}"
            elif chain_affinity == "eth":
                name = self._chain_code_to_chain_name(coin)
            else:
                name = "btc-derived-%s" % purpose
        temp_path = helpers.get_temp_file()
        path = self._wallet_path(temp_path)
        if exists(path):
            raise FileAlreadyExist()
        storage = WalletStorage(path)
        db = WalletDB("", manual_upgrades=False)
        if chain_affinity == "eth":
            ks = keystore.from_bip39_seed(seed, passphrase, util.get_keystore_path(bip39_derivation))
            db.put("keystore", ks.dump())
            db.put("wallet_type", f"{coin}_standard")
            wallet = Standard_Eth_Wallet(db, storage, config=self.config, index=account_id)
        elif chain_affinity == "btc":
            ks = keystore.from_bip39_seed(seed, passphrase, bip39_derivation)
            db.put("keystore", ks.dump())
            wallet = Standard_Wallet(db, storage, config=self.config)
        else:
            raise UnsupportedCurrencyCoin()
        wallet.hide_type = True
        wallet.set_name(name)
        wallet.coin = coin
        wallet.storage.set_path(self._wallet_path(wallet.identity))
        self.recovery_wallets[wallet.identity] = self.update_recovery_wallet(
            self.get_hd_wallet_encode_seed(seed=seed, coin=coin, purpose=str(purpose)),
            wallet,
            bip39_derivation,
            name,
            coin,
        )
        wallet.update_password(old_pw=None, new_pw=password, str_pw=self.android_id, encrypt_storage=True)
        if coin == "btc":
            wallet.start_network(self.daemon.network)
        wallet.save_db()

    def get_wallet_derivation_path(self, address: str) -> str:
        """
        Get the current wallet derivation path
        :param address: receiving address of the wallet
        :return: path as string
        """
        self._assert_wallet_isvalid()
        derivation_path = self.wallet.get_derivation_path(address)
        out_data = {"coin": self.wallet.coin, "path": derivation_path, "type": ""}
        if _get_chain_affinity(self.wallet.coin) == "btc":
            script_type = self.wallet.get_txin_type(address)
            add_type = [str(k) for k, v in PURPOSE_TO_ADDRESS_TYPE.items() if v == script_type]
            add_type = add_type[0] if add_type else '49'
            out_data.update({"type": add_type})

        return json.dumps(out_data)

    def get_devired_num(self, coin="btc"):
        """
        Get devired HD num by app
        :param coin:btc/eth as string
        :return: num as int
        """
        derived_num = 0
        coin = coin.lower()
        chain_affinity = _get_chain_affinity(coin)
        if is_coin_migrated(coin):
            return wallet_manager.count_primary_wallet_by_chain(coin_manager.legacy_coin_to_chain_code(coin))
        elif chain_affinity == "btc":
            for add_type in ["49", "84", "44"]:
                xpub = self.get_hd_wallet_encode_seed(coin=coin, purpose=add_type)
                derived_num += self.wallet_context.get_derived_num(xpub)
        elif chain_affinity == "eth":
            xpub = self.get_hd_wallet_encode_seed(coin=coin, purpose="44")
            derived_num = self.wallet_context.get_derived_num(xpub)
        return derived_num

    def get_account_id(self, path, coin):
        chain_code = coin
        chain_info = coin_manager.get_chain_info(chain_code)
        return helpers.get_path_info(path, bip44.BIP44Level[chain_info.bip44_auto_increment_level.upper()])

    def update_devired_wallet_info(self, bip39_derivation, xpub, name, coin):
        account_id = self.get_account_id(bip39_derivation, coin)
        self.wallet_context.add_derived_wallet(xpub, name, account_id)

    def get_all_mnemonic(self):
        """
        Get all mnemonic, num is 2048
        :return: json like "[job, ...]"
        """
        return json.dumps(Wordlist.from_file("english.txt"))

    @api.api_entry()
    def get_all_wallet_balance(self):
        """
        Get all wallet balances
        :return:
        {
          "all_balance": "21,233.46 CNY",
          "btc_asset":"",
          "wallet_info": [
            {
              "name": "",
              "coin":""
              "label": "",
              "sum_fiat": "1,333.55",
              "wallets": [
                {"coin": "btc", "address":"", "balance": "", "fiat": "", "icon":""}
              ]
            },
            {
              "name": "",
              "coin":""
              "label": "",
              "sum_fiat": "",
              "wallets": [
                { "coin": "btc", "address":"", "balance": "", "fiat": "", "icon":""},
                { "coin": "usdt", "address":"", "balance": "", "fiat": "", "icon":""}
              ]
            }
          ]
        }
        """
        out = {}
        try:
            all_balance = Decimal("0")
            all_wallet_info = []
            for wallet in self.daemon.get_wallets().values():
                wallet_info = {"name": wallet.identity, "label": wallet.get_name()}
                coin = wallet.coin
                wallet_info["coin"] = coin
                chain_affinity = _get_chain_affinity(coin)
                if is_coin_migrated(coin):
                    main_balance_info, contracts_balance_info, sum_fiat = self._get_general_wallet_all_balance(wallet)
                    wallet_info["wallets"] = [main_balance_info, *contracts_balance_info]
                    wallet_info["sum_fiat"] = sum_fiat
                    all_balance += sum_fiat
                    all_wallet_info.append(wallet_info)
                elif chain_affinity == "eth":
                    main_balance_info, contracts_balance_info, sum_fiat = self._get_eth_wallet_all_balance(wallet)
                    wallet_info["wallets"] = [main_balance_info] + contracts_balance_info
                    wallet_info["sum_fiat"] = sum_fiat
                    all_balance += sum_fiat
                    all_wallet_info.append(wallet_info)
                elif chain_affinity == "btc":
                    c, u, x = wallet.get_balance()
                    balance = c + u
                    fiat = Decimal(balance) / COIN * price_manager.get_last_price(coin, self.ccy)
                    fiat_str = f"{self.daemon.fx.ccy_amount_str(fiat, True)} {self.ccy}"
                    all_balance += fiat
                    wallet_info["wallets"] = [
                        {
                            "coin": "btc",
                            "address": wallet.get_addresses()[0],
                            "balance": self.format_amount(balance),
                            "fiat": fiat_str,
                            "icon": self._get_icon_by_token(coin),
                        }
                    ]
                    wallet_info["sum_fiat"] = fiat
                    all_wallet_info.append(wallet_info)
                else:
                    raise UnsupportedCurrencyCoin()

            no_zero_balance_wallets = (i for i in all_wallet_info if i["sum_fiat"] > 0)
            no_zero_balance_wallets = sorted(
                no_zero_balance_wallets, key=lambda i: i["sum_fiat"], reverse=True
            )  # sort no-zero balance wallet by fiat currency in reverse order

            zero_balance_wallets = (i for i in all_wallet_info if i["sum_fiat"] <= 0)
            zero_balance_wallets_dict = {i["name"]: i for i in zero_balance_wallets}
            sorted_wallet_labels = (i[0] for i in self.wallet_context.get_stored_wallets_types())

            zero_balance_wallets = [
                zero_balance_wallets_dict[i] for i in sorted_wallet_labels if i in zero_balance_wallets_dict
            ]  # sort zero balance wallet by created time in reverse order

            all_wallet_info = [*no_zero_balance_wallets, *zero_balance_wallets]

            out["all_balance"] = f"{self.daemon.fx.ccy_amount_str(all_balance, True)} {self.ccy}"
            out["btc_asset"] = self._fill_balance_info_with_coin(all_balance, "btc")
            out["wallet_info"] = all_wallet_info
            return json.dumps(out, cls=json_encoders.DecimalEncoder)
        except BaseException as e:
            raise e

    def set_rbf(self, status_rbf):
        """
        Enable/disable rbf
        :param status_rbf:True/False as bool
        :return:
        """
        use_rbf = self.config.get("use_rbf", True)
        if use_rbf == status_rbf:
            return
        self.config.set_key("use_rbf", status_rbf)
        self.rbf = status_rbf

    def get_rbf_status(self, tx_hash):
        try:
            tx = self.wallet.db.get_transaction(tx_hash)
            if not tx:
                return False
            height = self.wallet.get_tx_height(tx_hash).height
            _, is_mine, v, fee = self.wallet.get_wallet_delta(tx)
            is_unconfirmed = height <= 0
            if tx:
                # note: the current implementation of RBF *needs* the old tx fee
                rbf = is_mine and self.rbf and fee is not None and is_unconfirmed
                if rbf:
                    return True
                else:
                    return False
        except BaseException as e:
            raise e

    def format_fee_rate(self, fee_rate):
        # fee_rate is in sat/kB
        return util.format_fee_satoshis(fee_rate / 1000, num_zeros=self.num_zeros) + " sat/byte"

    def get_rbf_fee_info(self, tx_hash):
        tx = self.wallet.db.get_transaction(tx_hash)
        if not tx:
            raise BaseException(FailedGetTx())
        txid = tx.txid()
        assert txid
        fee = self.wallet.get_tx_fee(txid)
        if fee is None:
            raise BaseException(
                _("RBF(Replace-By-Fee) fails because it does not get the fee intormation of the original transaction.")
            )
        tx_size = tx.estimated_size()
        old_fee_rate = fee / tx_size  # sat/vbyte
        new_rate = Decimal(max(old_fee_rate * 1.5, old_fee_rate + 1)).quantize(Decimal("0.0"))
        new_tx = json.loads(self.create_bump_fee(tx_hash, str(new_rate)))
        ret_data = {
            "current_feerate": self.format_fee_rate(1000 * old_fee_rate),
            "new_feerate": str(new_rate),
            "fee": new_tx["fee"],
            "tx": new_tx["new_tx"],
        }
        return json.dumps(ret_data)

    # TODO:new_tx in history or invoices, need test

    def create_bump_fee(self, tx_hash, new_fee_rate):
        try:
            tx = self.wallet.db.get_transaction(tx_hash)
            if not tx:
                return False
            coins = self.wallet.get_spendable_coins(None, nonlocal_only=False)
            new_tx = self.wallet.bump_fee(tx=tx, new_fee_rate=new_fee_rate, coins=coins)
            fee = new_tx.get_fee()
        except BaseException as e:
            raise BaseException(e)

        new_tx.set_rbf(self.rbf)
        out = {"new_tx": str(new_tx), "fee": fee}
        self.rbf_tx = new_tx
        return json.dumps(out)

    def confirm_rbf_tx(self, tx_hash):
        try:
            self.do_save(self.rbf_tx)
        except BaseException:
            log_info.info("do save failed {}".format(self.rbf_tx))
            pass
        try:
            if self.label_flag and self.wallet.wallet_type != "standard":
                self.label_plugin.push_tx(
                    self.wallet, "rbftx", self.rbf_tx.txid(), str(self.rbf_tx), tx_hash_old=tx_hash
                )
        except Exception as e:
            log_info.info("push_tx rbftx error {}".format(e))
            pass
        return self.rbf_tx

    def get_rbf_or_cpfp_status(self, tx_hash):
        try:
            status = {}
            tx = self.wallet.db.get_transaction(tx_hash)
            if not tx:
                raise BaseException("tx is None")
            tx_details = self.wallet.get_tx_info(tx)
            is_unconfirmed = tx_details.tx_mined_status.height <= 0
            if is_unconfirmed and tx:
                # note: the current implementation of rbf *needs* the old tx fee
                if tx_details.can_bump and tx_details.fee is not None:
                    status["rbf"] = True
                else:
                    child_tx = self.wallet.cpfp(tx, 0)
                    if child_tx:
                        status["cpfp"] = True
            return json.dumps(status)
        except BaseException as e:
            raise e

    def get_cpfp_info(self, tx_hash, suggested_feerate=None):
        try:
            self._assert_wallet_isvalid()
            parent_tx = self.wallet.db.get_transaction(tx_hash)
            if not parent_tx:
                raise BaseException(FailedGetTx())
            info = {}
            child_tx = self.wallet.cpfp(parent_tx, 0)
            if child_tx:
                total_size = parent_tx.estimated_size() + child_tx.estimated_size()
                parent_txid = parent_tx.txid()
                assert parent_txid
                parent_fee = self.wallet.get_tx_fee(parent_txid)
                if parent_fee is None:
                    raise BaseException(
                        _(
                            "CPFP(Child Pays For Parent) fails because it does not get the fee intormation of the original transaction."
                        )
                    )
                info["total_size"] = "(%s) bytes" % total_size
                max_fee = child_tx.output_value()
                info["input_amount"] = self.format_amount(max_fee) + " " + self.base_unit

                def get_child_fee_from_total_feerate(fee_per_kb):
                    fee = fee_per_kb * total_size / 1000 - parent_fee
                    fee = min(max_fee, fee)
                    fee = max(total_size, fee)  # pay at least 1 sat/byte for combined size
                    return fee

                if suggested_feerate is None:
                    suggested_feerate = self.config.fee_per_kb()
                else:
                    suggested_feerate = suggested_feerate * 1000
                if suggested_feerate is None:
                    raise BaseException(
                        f"""{_("Failed CPFP(Child Pays For Parent)'")}: {_('dynamic fee estimates not available')}"""
                    )

                parent_feerate = parent_fee / parent_tx.estimated_size() * 1000
                info["parent_feerate"] = self.format_fee_rate(parent_feerate) if parent_feerate else ""
                info["fee_rate_for_child"] = self.format_fee_rate(suggested_feerate) if suggested_feerate else ""
                fee_for_child = get_child_fee_from_total_feerate(suggested_feerate)
                info["fee_for_child"] = util.format_satoshis_plain(fee_for_child, decimal_point=self.decimal_point)
                if fee_for_child is None:
                    raise BaseException("fee_for_child is none")
                out_amt = max_fee - fee_for_child
                out_amt_str = (self.format_amount(out_amt) + " " + self.base_unit) if out_amt else ""
                info["output_amount"] = out_amt_str
                comb_fee = parent_fee + fee_for_child
                comb_fee_str = (self.format_amount(comb_fee) + " " + self.base_unit) if comb_fee else ""
                info["total_fee"] = comb_fee_str
                comb_feerate = comb_fee / total_size * 1000
                comb_feerate_str = self.format_fee_rate(comb_feerate) if comb_feerate else ""
                info["total_feerate"] = comb_feerate_str

                if fee_for_child is None:
                    raise BaseException(_("Sub-transaction fee error."))  # fee left empty, treat is as "cancel"
                if fee_for_child > max_fee:
                    raise BaseException(_("Exceeding the Maximum fee limit."))
            return json.dumps(info)
        except BaseException as e:
            raise e

    def create_cpfp_tx(self, tx_hash, fee_for_child):
        try:
            self._assert_wallet_isvalid()
            parent_tx = self.wallet.db.get_transaction(tx_hash)
            if not parent_tx:
                raise BaseException(FailedGetTx())
            new_tx = self.wallet.cpfp(parent_tx, self.get_amount(fee_for_child))
            new_tx.set_rbf(self.rbf)
            out = {"new_tx": str(new_tx)}

            try:
                self.do_save(new_tx)
                if self.label_flag and self.wallet.wallet_type != "standard":
                    self.label_plugin.push_tx(self.wallet, "createtx", new_tx.txid(), str(new_tx))
            except BaseException as e:
                log_info.info("push_tx createtx error {}".format(e))
                pass
            return json.dumps(out)
        except BaseException as e:
            raise e

    def get_default_server(self):
        """
        Get default electrum server
        :return: json like {'host':'127.0.0.1', 'port':'3456'}
        """
        try:
            self._assert_daemon_running()
            net_params = self.network.get_parameters()
            host, port = net_params.server.host, net_params.server.port
        except BaseException as e:
            raise e

        default_server = {
            "host": host,
            "port": port,
        }
        return json.dumps(default_server)

    def set_server(self, host, port):
        """
        Custom server
        :param host: host as str
        :param port: port as str
        :return: raise except if error
        """
        try:
            self._assert_daemon_running()
            net_params = self.network.get_parameters()
            try:
                server = ServerAddr.from_str_with_inference("%s:%s" % (host, port))
                if not server:
                    raise Exception("failed to parse")
            except BaseException as e:
                raise e
            net_params = net_params._replace(server=server, auto_connect=True)
            self.network.run_from_another_thread(self.network.set_parameters(net_params))
        except BaseException as e:
            raise e

    def get_server_list(self):
        """
        Get Servers
        :return: servers info as json
        """
        try:
            self._assert_daemon_running()
            servers = self.daemon.network.get_servers()
        except BaseException as e:
            raise e
        return json.dumps(servers)

    def rename_wallet(self, old_name, new_name):
        """
        Rename the wallet
        :param old_name: old name as string
        :param new_name: new name as string
        :return: raise except if error
        """
        try:
            self._assert_daemon_running()
            if old_name is None or new_name is None:
                raise BaseException(("Please enter the correct file name."))
            else:
                wallet = self.daemon.get_wallet(self._wallet_path(old_name))
                wallet.set_name(new_name)
                wallet.db.set_modified(True)
                wallet.save_db()
        except BaseException as e:
            raise e

    def update_wallet_name(self, old_name, new_name):
        try:
            self._assert_daemon_running()
            if old_name is None or new_name is None:
                raise BaseException("Please enter the correct file name")
            else:
                os.rename(self._wallet_path(old_name), self._wallet_path(new_name))
                self.daemon.pop_wallet(self._wallet_path(old_name))
                self.load_wallet(new_name, password=self.android_id)
                self.select_wallet(new_name)
                return new_name
        except BaseException as e:
            raise e

    def switch_wallet(self, name):
        """
        Switching to a specific wallet
        :param name: name as string
        :return: json like
        {
            "name": "",
            "label": "",
            "wallets": [
            {"coin": "usdt", "address": ""},
            ...
            ]
        }
        """
        self._assert_daemon_running()
        if name is None:
            raise FailedToSwitchWallet()

        self.wallet = self.daemon.get_wallet(self._wallet_path(name))
        # self.wallet.use_change = self.config.get("use_change", False)
        chain_affinity = _get_chain_affinity(self.wallet.coin)
        if is_coin_migrated(self.wallet.coin) and isinstance(self.wallet, GeneralWallet):
            tokens = self.wallet.get_all_token_coins()
            info = {
                "name": name,
                "label": self.wallet.get_name(),
                "wallets": [{"coin": i.symbol, "address": i.token_address, "coin_code": i.code} for i in tokens],
            }
        elif chain_affinity == "eth":
            contract_info = self.wallet.get_contract_symbols_with_address()

            info = {"name": name, "label": self.wallet.get_name(), "wallets": contract_info}
        elif chain_affinity == "btc":
            if not isinstance(self.wallet, Imported_Wallet):
                self.wallet.set_key_pool_size()
            util.trigger_callback("wallet_updated", self.wallet)

            info = {
                "name": name,
                "label": self.wallet.get_name(),
                "wallets": [],
            }
            if self.label_flag and self.wallet.wallet_type != "standard":
                self.label_plugin.load_wallet(self.wallet)
        else:
            raise UnsupportedCurrencyCoin()
        return json.dumps(info, cls=json_encoders.DecimalEncoder)

    @api.api_entry()
    def get_wallet_balance(self):
        """
        Get the current balance of your wallet
        :return: json like
        {
          "all_balance": "",
          "coin_asset":"",
          "coin":"",
          "name":"",
          "wallets": [
            {"coin": "eth", "address": "", "balance": "", "fiat": "", "icon":""},
            {"coin": "usdt", "address": "", "balance": "", "fiat": "", "icon":""}
          ]
        }
        """
        self._assert_wallet_isvalid()
        coin = self.wallet.coin
        info = {"name": self.wallet.identity, "label": self.wallet.get_name(), "coin": coin}
        chain_affinity = _get_chain_affinity(coin)
        if is_coin_migrated(coin):
            main_balance_info, contracts_balance_info, sum_fiat = self._get_general_wallet_all_balance(self.wallet)
            info.update(
                {
                    "all_balance": f"{self.daemon.fx.ccy_amount_str(sum_fiat, True)} {self.ccy}",
                    "wallets": [main_balance_info] + contracts_balance_info,
                    "coin_asset": self._fill_balance_info_with_coin(sum_fiat, coin)
                    if sum_fiat != Decimal(0)
                    else main_balance_info["balance"],
                }
            )
        elif chain_affinity == "eth":
            main_balance_info, contracts_balance_info, sum_fiat = self._get_eth_wallet_all_balance(self.wallet)
            info.update(
                {
                    "all_balance": f"{self.daemon.fx.ccy_amount_str(sum_fiat, True)} {self.ccy}",
                    "wallets": [main_balance_info] + contracts_balance_info,
                    "coin_asset": self._fill_balance_info_with_coin(sum_fiat, coin)
                    if sum_fiat != Decimal(0)
                    else main_balance_info["balance"],
                }
            )
        elif chain_affinity == "btc":
            c, u, x = self.wallet.get_balance()
            balance = c + u
            fiat = Decimal(balance) / COIN * price_manager.get_last_price(coin, self.ccy)
            fiat_str = f"{self.daemon.fx.ccy_amount_str(fiat, True)} {self.ccy}"
            info.update(
                {
                    "all_balance": fiat_str,  # fixme deprecated field
                    "wallets": [
                        {
                            "coin": "btc",
                            "address": self.wallet.get_addresses()[0],
                            "balance": self.format_amount(balance),
                            "icon": self._get_icon_by_token(coin),
                            "fiat": fiat_str,
                        }
                    ],
                    "coin_asset": self.format_amount(balance),
                }
            )
            if self.label_flag and self.wallet.wallet_type != "standard":
                self.label_plugin.load_wallet(self.wallet)
        else:
            raise UnsupportedCurrencyCoin()
        return json.dumps(info, cls=json_encoders.DecimalEncoder)

    def select_wallet(self, name):  # TODO: Will be deleted later
        """
        Select wallet by name
        :param name: name as string
        :return: json like
        {
          "name": "",
          "label": "",
          "wallets": [
            {"coin": "eth", "balance": "", "fiat": ""},
            {"coin": "usdt", "balance": "", "fiat": ""}
          ]
        }
        """
        self._assert_daemon_running()
        if name is None:
            self.wallet = None
        else:
            self.wallet = self.daemon.get_wallet(self._wallet_path(name))

        self.wallet.use_change = self.config.get("use_change", False)
        coin = self.wallet.coin
        chain_affinity = _get_chain_affinity(coin)
        if is_coin_migrated(coin):
            main_balance_info, contracts_balance_info, _ = self._get_general_wallet_all_balance(self.wallet)
            info = {
                "name": name,
                "label": self.wallet.get_name(),
                "wallets": [main_balance_info] + contracts_balance_info,
            }
            return json.dumps(info, cls=json_encoders.DecimalEncoder)
        elif chain_affinity == "eth":
            main_balance_info, contracts_balance_info, _zero_fiat = self._get_eth_wallet_all_balance(
                self.wallet, with_sum_fiat=False
            )
            info = {
                "name": name,
                "label": self.wallet.get_name(),
                "wallets": [main_balance_info] + contracts_balance_info,
            }
            return json.dumps(info, cls=json_encoders.DecimalEncoder)
        elif chain_affinity == "btc":
            c, u, x = self.wallet.get_balance()
            util.trigger_callback("wallet_updated", self.wallet)

            balance = c + u
            fiat = Decimal(balance) / COIN * price_manager.get_last_price(coin, self.ccy)
            fiat_str = f"{self.daemon.fx.ccy_amount_str(fiat, True)} {self.ccy}"
            info = {
                "balance": self.format_amount(balance) + " (%s)" % fiat_str,  # fixme deprecated field
                "name": name,
                "label": self.wallet.get_name(),
                "wallets": [{"coin": "btc", "balance": self.format_amount(balance), "fiat": fiat_str}],
            }
            if self.label_flag and self.wallet.wallet_type != "standard":
                self.label_plugin.load_wallet(self.wallet)
            return json.dumps(info, cls=json_encoders.DecimalEncoder)
        else:
            raise UnsupportedCurrencyCoin()

    def _get_general_wallet_all_balance(self, wallet: Optional[GeneralWallet]) -> Tuple[dict, list, Decimal]:
        wallet = wallet or self.wallet
        assert isinstance(wallet, GeneralWallet)

        sum_fiat = Decimal('0')
        assets = wallet.get_all_balance()
        chain_code = wallet.chain_code

        balances_info = {}
        _sort_helper_dict = {}
        for coin_code, asset in assets.items():
            price = price_manager.get_last_price(coin_code, self.ccy)
            balance_with_decimals = Decimal(asset["balance"]) / pow(10, Decimal(asset["decimals"]))
            fiat = balance_with_decimals * price
            sum_fiat += fiat

            balance_info = {
                "coin": asset["symbol"],  # The key is misleading, should be symbol
                "address": asset["token_address"] or asset["address"],
                "icon": asset["icon"],
                "balance": balance_with_decimals,
                "fiat": f"{self.daemon.fx.ccy_amount_str(fiat, True)} {self.ccy}",
            }
            balances_info[coin_code] = balance_info
            if coin_code != chain_code:
                _sort_helper_dict[asset["token_address"]] = fiat

        ret_main_balance_info = balances_info.pop(chain_code)
        ret_contracts_balance_info = sorted(
            balances_info.values(), key=lambda i: (_sort_helper_dict.get(i["address"]), i["balance"]), reverse=True
        )
        return ret_main_balance_info, ret_contracts_balance_info, sum_fiat

    def _get_eth_wallet_all_balance(
        self, wallet: Optional[Abstract_Eth_Wallet] = None, with_sum_fiat: bool = True
    ) -> Tuple[Dict, List, Decimal]:
        wallet = wallet or self.wallet
        chain_code = wallet.coin
        main_balance, tokens_balance_info = wallet.get_all_balance()
        sum_fiat = Decimal('0')

        main_coin_code = coin_manager.get_chain_info(chain_code).fee_code
        main_coin = coin_manager.get_coin_info(main_coin_code)
        main_coin_price = price_manager.get_last_price(main_coin_code, self.ccy)
        main_coin_fiat = main_balance * main_coin_price
        if with_sum_fiat:
            sum_fiat += main_coin_fiat
        ret_main_balance_info = {
            "coin": main_coin.symbol,  # The key is misleading, should be symbol
            "address": wallet.get_addresses()[0],
            "icon": main_coin.icon,
            "balance": main_balance,
            "fiat": f"{self.daemon.fx.ccy_amount_str(main_coin_fiat, True)} {self.ccy}",
        }

        ret_contracts_balance_info = []
        _sort_helper_dict = {}

        token_addresses = list(tokens_balance_info.keys())
        for token in coin_manager.query_coins_by_token_addresses(chain_code, token_addresses):
            token_address = token.token_address.lower()
            token_balance = Decimal(tokens_balance_info.get(token_address)) / pow(10, token.decimals)
            price = price_manager.get_last_price(token.code, self.ccy)
            fiat = token_balance * price
            fiat_str = f"{self.daemon.fx.ccy_amount_str(fiat, True)} {self.ccy}"
            if with_sum_fiat:
                sum_fiat += fiat

            ret_contracts_balance_info.append(
                {
                    "coin": token.symbol,  # The key is misleading, should be symbol
                    "address": token.token_address,
                    "icon": token.icon,
                    "balance": token_balance,
                    "fiat": fiat_str,
                }
            )
            _sort_helper_dict[token_address] = fiat

        ret_contracts_balance_info.sort(
            key=lambda balance_info: (_sort_helper_dict.get(balance_info["address"]), balance_info["balance"]),
            reverse=True,
        )
        return ret_main_balance_info, ret_contracts_balance_info, sum_fiat

    def _fill_balance_info_with_coin(self, fiat: Decimal, coin: str) -> str:
        price = price_manager.get_last_price(coin, self.ccy)
        chain_affinity = _get_chain_affinity(coin)
        if price == 0:
            return "0"

        if chain_affinity == "btc":
            return self.format_amount((int(Decimal(fiat) / Decimal(price) * COIN)))
        else:
            return Decimal(fiat) / Decimal(price)

    def set_wallet_location_info(self, wallet_location_info: list, wallet_type="btc") -> None:
        """
        Save a user-defined order of wallets according to the type of wallet
        :param wallet_location_info: customised wallet order
        :param wallet_type: hd/hw/btc/eth/bsc/heco
        :return: None
        """
        self.wallet_context.set_wallet_location_info(wallet_location_info, wallet_type)

    def list_wallets(self, type_=None):
        """
        List available wallets
        :param type: None/hw/hd/btc/eth/bsc/heco/okt
        :return: json like "[{"wallet_key":{'type':"", "addr":"", "name":"", "label":"", "device_id": ""}}, ...]"
        exp:
            all_list = testcommond.list_wallets()
            hd_list = testcommond.list_wallets(type='hd')
            hw_list = testcommond.list_wallets(type='hw')
            btc_list = testcommond.list_wallets(type='btc')
            eth_list = testcommond.list_wallets(type='eth')

        """
        coin = None
        generic_wallet_type = None
        if type_ in ("hw", "hd"):
            generic_wallet_type = type_
        else:
            coin = type_

        wallet_infos = []
        for wallet_id, wallet_type in self.wallet_context.get_stored_wallets_types(generic_wallet_type, coin):
            wallet = self.daemon.get_wallet(self._wallet_path(wallet_id))
            if isinstance(wallet.keystore, Hardware_KeyStore):
                device_id = wallet.get_device_info()
            else:
                device_id = ""

            wallet_infos.append(
                {
                    wallet_id: {
                        "type": wallet_type,
                        "addr": wallet.get_addresses()[0],
                        "name": wallet.identity,
                        "label": wallet.get_name(),
                        "device_id": device_id,
                        "derivation_path": wallet.get_derivation_path(wallet.get_addresses()[0]),
                    }
                }
            )
        return json.dumps(wallet_infos)

    def delete_wallet_from_deamon(self, name):
        try:
            self._assert_daemon_running()
            self.daemon.delete_wallet(name)
        except BaseException as e:
            raise BaseException(e)

    def reset_config_info(self):
        self.wallet_context.clear_type_info()
        self.wallet_context.clear_derived_info()
        self.wallet_context.clear_backup_info()
        self.decimal_point = 5
        self.config.set_key("decimal_point", self.decimal_point)
        self.config.set_key("language", "zh_CN")
        self.config.set_key("sync_server_host", "39.105.86.163:8080")
        self.config.set_key("show_addr_info", {})

    def reset_wallet_info(self):
        """
        Reset all wallet info when Reset App
        :return: raise except if error
        """
        try:
            the_begging.reset_runtime()
            util.delete_file(self._wallet_path())
            util.delete_file(self._tx_list_path())
            self.reset_config_info()
            self.hd_wallet = None
            self.check_pw_wallet = None
            self.daemon._wallets.clear()
        except BaseException as e:
            raise e

    def delete_wallet_derived_info(self, wallet_obj, hw=False):
        coin = wallet_obj.coin
        chain_affinity = _get_chain_affinity(coin)
        if chain_affinity == "eth":
            # TODO: try implementing get_history for eth wallets.
            chain_code = coin
            try:
                address_info = provider_manager.get_address(chain_code, wallet_obj.get_addresses()[0])
            except Exception:
                return
            wallet_has_history = address_info.existing
        elif chain_affinity == "btc":
            wallet_has_history = bool(wallet_obj.get_history())
        else:
            return

        if wallet_has_history:
            return

        # delete wallet info from config
        derivation = wallet_obj.get_derivation_path(wallet_obj.get_addresses()[0])
        account_id = self.get_account_id(derivation, coin)

        if hw:
            if coin == "btc":
                xpub = wallet_obj.get_derived_master_xpub() + 'btc'
            else:
                xpub = wallet_obj.keystore.xpub + coin.lower()
        else:
            purpose = helpers.get_path_info(derivation, PURPOSE_POS)
            xpub = self.get_hd_wallet_encode_seed(coin=coin, purpose=purpose)

        self.wallet_context.remove_derived_wallet(xpub, account_id)

    @api.api_entry()
    @orm_database.db.atomic()
    def delete_wallet(self, password="", name="", hd=None):
        """
        Delete (a/all hd) wallet
        :param password: Password as string
        :param name: Wallet key
        :param hd: True if you want to delete all hd wallet
        :return: None
        """

        wallet = self.daemon.get_wallet(self._wallet_path(name))

        if not wallet.is_watching_only() and not self.wallet_context.is_hw(name):
            self.check_password(password=password)

        if hd is not None:
            self.delete_derived_wallet()
            wallet_manager.clear_all_primary_wallets(password)
        else:
            if isinstance(wallet, GeneralWallet):
                wallet.delete_wallet(password)

            if self.wallet_context.is_derived(name):
                hw = self.wallet_context.is_hw(name)
                self.delete_wallet_derived_info(wallet, hw=hw)
            self.delete_wallet_from_deamon(self._wallet_path(name))
            self.wallet_context.remove_type_info(name)
            # os.remove(self._wallet_path(name))

    def stc_spec_get_receipt_identifier_address(self, wallet_name):
        wallet = self.get_wallet_by_name(wallet_name)
        assert isinstance(wallet, GeneralWallet)
        return wallet.stc_spec_get_receipt_identifier_address()

    def _assert_daemon_running(self):
        if not self.daemon_running:
            raise BaseException(
                _("The background process does not start and it is recommended to restart the application.")
            )
            # Same wording as in electrum script.

    def _assert_wizard_isvalid(self):
        if self.wizard is None:
            raise BaseException("Wizard not running")
            # Log callbacks on stderr so they'll appear in the console activity.

    def _assert_wallet_isvalid(self):
        if self.wallet is None:
            raise util.NotChosenWallet()
            # Log callbacks on stderr so they'll appear in the console activity.

    def _assert_hd_wallet_isvalid(self):
        if self.hd_wallet is None:
            raise BaseException(UnavaiableHdWallet())

    def _wallet_path(self, name=""):
        if name is None:
            if not self.wallet:
                raise ValueError("No wallet selected")
            return self.wallet.storage.path
        else:
            wallets_dir = join(self.user_dir, "wallets")
            util.make_dir(wallets_dir)
            return util.standardize_path(join(wallets_dir, name))

    def _tx_list_path(self, name=""):
        wallets_dir = join(self.user_dir, "tx_history")
        util.make_dir(wallets_dir)
        return util.standardize_path(join(wallets_dir, name))

    @api.api_entry(force_version=api.Version.V3)
    def network_list(self, params):
        refresh = params.get("refresh", False)
        _ = chains_config.list_chain_settings(refresh=refresh)
        return []  # TODO


all_commands = commands.known_commands.copy()
for name, func in vars(AndroidCommands).items():
    if not name.startswith("_"):
        all_commands[name] = commands.Command(func, "")

SP_SET_METHODS = {
    bool: "putBoolean",
    float: "putFloat",
    int: "putLong",
    str: "putString",
}
