import base64
from typing import Dict, Optional, Tuple, Union

import base58
import spl.token.instructions as spl_token
from solana import system_program
from solana.blockhash import Blockhash
from solana.publickey import PublicKey
from solana.transaction import SIG_LENGTH, SigPubkeyPair, Transaction
from solana.utils.ed25519_base import is_on_curve
from spl.token.constants import TOKEN_PROGRAM_ID

from electrum_gui.common.basic.functional.require import require, require_not_none
from electrum_gui.common.provider.data import AddressValidation, SignedTx, UnsignedTx
from electrum_gui.common.provider.interfaces import ProviderInterface
from electrum_gui.common.secret.interfaces import SignerInterface, VerifierInterface


class SOLProvider(ProviderInterface):
    """Solana provider."""

    def verify_address(self, address: str) -> AddressValidation:
        """Basic verification only encoding and length"""
        is_valid = False
        try:
            _ = PublicKey(address)
            is_valid = True
        except ValueError:
            pass
        return AddressValidation(is_valid=is_valid, display_address=address, normalized_address=address)

    def pubkey_to_address(self, verifier: VerifierInterface, encoding: str = None) -> str:
        address = PublicKey(verifier.get_pubkey())
        return str(address)

    def fill_unsigned_tx(self, unsigned_tx: UnsignedTx) -> UnsignedTx:

        fee_price_per_unit = unsigned_tx.fee_price_per_unit or self.client.get_prices_per_unit_of_fee().normal.price
        tx_input = unsigned_tx.inputs[0] if unsigned_tx.inputs else None
        tx_output = unsigned_tx.outputs[0] if unsigned_tx.outputs else None
        payload = unsigned_tx.payload.copy()
        fee_limit = 1  # len(transfer_tx.signatures)
        if tx_input is not None and tx_output is not None:
            token_address = unsigned_tx.outputs[0].token_address
            receiver = unsigned_tx.outputs[0].address
            # note: method `is_on_curve` may has risk. see https://github.com/solana-labs/solana/issues/17106
            is_valid_system_account = is_on_curve(base58.b58decode(receiver))
            # spl-token transfer
            if token_address:
                payload["is_token_account"] = False
                account_info = self.client.get_account_info(receiver)
                # account not funded: only support system account
                if account_info is None:
                    require(is_valid_system_account, "only not_funded system account allowed")
                else:
                    if is_valid_system_account:
                        require(
                            account_info["owner"] == str(spl_token.SYS_PROGRAM_ID),
                            f"system account with invalid owner {account_info['owner']}",
                        )
                    else:
                        # token account
                        require(account_info["owner"] == str(spl_token.TOKEN_PROGRAM_ID), "invalid account owner")
                        require(
                            account_info["data"]["parsed"]["info"]["mint"] == token_address,
                            f"invalid token account with {token_address}",
                        )
                        payload["account_funded"] = True
                        payload["is_token_account"] = True
                if is_valid_system_account:
                    token_receiver = spl_token.get_associated_token_address(
                        owner=PublicKey(receiver), mint=PublicKey(token_address)
                    )
                    account_info = self.client.get_account_info(str(token_receiver))
                    if account_info is None:
                        payload["account_funded"] = False
                    else:
                        payload["account_funded"] = True
            else:
                # sol transfer only support system accounts
                require(is_valid_system_account, "fall off curve pubkey is not allowed")
            # something like nonce
            _, recent_blockhash = self.client.get_fees()
            payload["recent_blockhash"] = recent_blockhash

        return unsigned_tx.clone(
            inputs=[tx_input] if tx_input is not None else [],
            outputs=[tx_output] if tx_output is not None else [],
            fee_limit=fee_limit,
            fee_price_per_unit=fee_price_per_unit,
            payload=payload,
        )

    def get_token_info_by_address(self, token_address: str) -> Tuple[str, str, int]:
        _ = PublicKey(token_address)
        token_info = self.client.get_account_info(token_address)
        require_not_none(token_info, "invalid token address")
        account_info = token_info["data"]["parsed"]
        require(account_info["type"] == "mint", "invalid token_address")
        decimals = account_info["info"]["decimals"]
        return token_address[:4].upper(), token_address[:4], decimals

    def sign_transaction(self, unsigned_tx: Union[UnsignedTx], signers: Dict[str, SignerInterface]) -> SignedTx:
        sender = unsigned_tx.inputs[0].address
        receiver = unsigned_tx.outputs[0].address
        amount = unsigned_tx.outputs[0].value
        token_address = unsigned_tx.outputs[0].token_address
        transfer_tx = _build_tx(sender, receiver, amount, unsigned_tx.payload, mint_address=token_address)
        signature_pair = SigPubkeyPair(pubkey=PublicKey(sender))
        transfer_tx.signatures.append(signature_pair)
        sign_data = transfer_tx.serialize_message()
        sig, _ = signers[sender].sign(sign_data)
        require(len(sig) == SIG_LENGTH, "signature has invalid length")
        signature_pair.signature = sig
        txid = base58.b58encode(sig).decode()
        raw_tx = base64.b64encode(transfer_tx.serialize()).decode()
        return SignedTx(txid=txid, raw_tx=raw_tx)


def _build_tx(
    from_addr: str, to_addr: str, amount: int, payload: dict, mint_address: Optional[str] = None
) -> Transaction:
    transfer_tx = Transaction()
    sender = PublicKey(from_addr)
    receiver = PublicKey(to_addr)
    if mint_address is None:
        # SOL transfer
        transfer_tx.add(
            system_program.transfer(
                system_program.TransferParams(from_pubkey=sender, to_pubkey=receiver, lamports=amount)
            )
        )
    else:
        # SPL-Token transfer
        token_sender = spl_token.get_associated_token_address(owner=sender, mint=PublicKey(mint_address))
        account_funded = payload["account_funded"]
        is_token_account = payload["is_token_account"]
        # account not funded
        if not account_funded:
            transfer_tx.add(
                spl_token.create_associated_token_account(
                    payer=sender,
                    owner=receiver,
                    mint=PublicKey(mint_address),
                )
            )
        if not is_token_account:
            receiver = spl_token.get_associated_token_address(owner=receiver, mint=PublicKey(mint_address))
        transfer_tx.add(
            spl_token.transfer(
                spl_token.TransferParams(
                    program_id=TOKEN_PROGRAM_ID,
                    source=token_sender,
                    dest=receiver,
                    owner=sender,
                    amount=amount,
                    signers=[sender],
                )
            )
        )
    transfer_tx.recent_blockhash = Blockhash(payload["recent_blockhash"])
    return transfer_tx
