import time
from typing import Dict, Tuple

from nacl.public import PublicKey

from electrum_gui.common.basic.functional.require import require
from electrum_gui.common.provider.chains.stc import STCJsonRPC
from electrum_gui.common.provider.chains.stc.sdk.serde_types import uint8, uint64, uint128
from electrum_gui.common.provider.chains.stc.sdk.starcoin_stdlib import encode_peer_to_peer_script_function
from electrum_gui.common.provider.chains.stc.sdk.starcoin_types import (
    ChainId,
    Identifier,
    RawTransaction,
    StructTag,
    TypeTag__Struct,
)
from electrum_gui.common.provider.chains.stc.sdk.utils import utils as stc_utils
from electrum_gui.common.provider.chains.stc.sdk.utils.auth_key import AuthKey
from electrum_gui.common.provider.chains.stc.sdk.utils.receipt_identifier import ReceiptIdentifier
from electrum_gui.common.provider.data import AddressValidation, SignedTx, UnsignedTx
from electrum_gui.common.provider.interfaces import ProviderInterface
from electrum_gui.common.secret.interfaces import SignerInterface, VerifierInterface


class _STCKey(object):
    def __init__(self, signer: SignerInterface):
        self.signer = signer

    def sign_msg_hash(self, data: bytes):
        sig, _ = self.signer.sign(data)
        return sig


class STCProvider(ProviderInterface):
    def verify_address(self, address: str) -> AddressValidation:
        is_valid, encoding = False, None

        if address.startswith("stc"):
            try:
                ReceiptIdentifier.decode(address)
            except BaseException:
                pass
            else:
                encoding = "BECH32"
                is_valid = True

        else:
            try:
                addr = stc_utils.account_address_hex(address)
            except stc_utils.InvalidAccountAddressError:
                pass
            else:
                if len(addr) == 32:
                    is_valid = True

        return AddressValidation(
            normalized_address=address if is_valid else "",
            display_address=address if is_valid else "",
            is_valid=is_valid,
            encoding=encoding,
        )

    def pubkey_to_address(self, verifier: VerifierInterface, encoding: str = None) -> str:
        require(encoding in ("HEX", "BECH32"))
        pubkey = verifier.get_pubkey(compressed=False)
        auth_key = AuthKey.from_public_key(PublicKey(pubkey))

        if encoding == "HEX":
            address = "0x" + stc_utils.account_address_hex(auth_key.account_address())
        elif encoding == "BECH32":
            address = ReceiptIdentifier(auth_key.account_address(), auth_key).encode()
        return address

    @property
    def stc_json_rpc(self) -> STCJsonRPC:
        return self.client_selector(instance_required=STCJsonRPC)

    def fill_unsigned_tx(self, unsigned_tx: UnsignedTx) -> UnsignedTx:
        fee_price_per_unit = unsigned_tx.fee_price_per_unit or self.client.get_prices_per_unit_of_fee().normal.price
        nonce = unsigned_tx.nonce
        payload = unsigned_tx.payload.copy()
        tx_input = unsigned_tx.inputs[0] if unsigned_tx.inputs else None
        tx_output = unsigned_tx.outputs[0] if unsigned_tx.outputs else None
        fee_limit = unsigned_tx.fee_limit or 100000

        if tx_input is not None and tx_output is not None:
            from_address = tx_input.address
            to_address = tx_output.address
            if to_address.startswith("stc"):
                ri = ReceiptIdentifier.decode(to_address)
                require(ri is not None)
                payee_auth_key = ri.auth_key
                to_address = ri.account_address
            else:
                payee_auth_key = None

            value = tx_output.value
            if tx_output.token_address:
                items = tx_output.token_address.split("::")
                require(len(items) == 3)
                token_address, token_module, token_name = items
            else:
                token_address, token_module, token_name = stc_utils.CORE_CODE_ADDRESS, "STC", "STC"

            if nonce is None:
                nonce = self.client.get_address(from_address).nonce

            if payload.get("expiration_time") is None:
                payload['expiration_time'] = int(time.time()) + 3600

            payload['txScript'] = encode_peer_to_peer_script_function(
                token_type=TypeTag__Struct(
                    value=StructTag(
                        address=stc_utils.account_address(token_address),
                        module=Identifier(token_module),
                        name=Identifier(token_name),
                        type_params=[],
                    )
                ),
                payee=stc_utils.account_address(to_address),
                payee_auth_key=payee_auth_key.data if payee_auth_key is not None else b"",
                amount=uint128(value),
            )

        return unsigned_tx.clone(
            inputs=[tx_input] if tx_input is not None else [],
            outputs=[tx_output] if tx_output is not None else [],
            fee_limit=fee_limit,
            fee_price_per_unit=fee_price_per_unit,
            nonce=nonce,
            payload=payload,
        )

    def sign_transaction(self, unsigned_tx: UnsignedTx, signers: Dict[str, SignerInterface]) -> SignedTx:
        # TODO: check whether main coin balance and token balance are enough
        require(len(unsigned_tx.inputs) == 1 and len(unsigned_tx.outputs) == 1)
        from_address = unsigned_tx.inputs[0].address
        require(signers.get(from_address) is not None)
        require(unsigned_tx.payload is not None)

        stc_key = _STCKey(signers[from_address])

        raw_txn = RawTransaction(
            sender=stc_utils.account_address(from_address),
            sequence_number=uint64(unsigned_tx.nonce),
            payload=unsigned_tx.payload.get("txScript"),
            max_gas_amount=uint64(unsigned_tx.fee_limit),
            gas_unit_price=uint64(unsigned_tx.fee_price_per_unit),
            gas_token_code="0x1::STC::STC",
            expiration_timestamp_secs=unsigned_tx.payload.get("expiration_time"),
            chain_id=ChainId(uint8(int(self.chain_info.chain_id))),
        )

        signature = stc_key.sign_msg_hash(stc_utils.raw_transaction_signing_msg(raw_txn))
        signed_tx = stc_utils.create_signed_transaction(raw_txn, signers[from_address].get_pubkey(), signature)
        tx_hash = stc_utils.hash(
            stc_utils.starcoin_hash_seed(b"SignedUserTransaction"), signed_tx.bcs_serialize()
        ).hex()
        return SignedTx(
            txid="0x" + tx_hash,
            raw_tx="0x" + signed_tx.bcs_serialize().hex(),
        )

    def get_token_info_by_address(self, token_address: str) -> Tuple[str, str, int]:
        raise NotImplementedError()
