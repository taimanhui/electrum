from typing import Optional

from electrum_gui.common.coin import manager as coin_manager
from electrum_gui.common.provider import manager as provider_manager
from electrum_gui.common.provider.data import TransactionInput, TransactionOutput, UnsignedTx
from electrum_gui.common.secret import manager as secret_manager
from electrum_gui.common.wallet import daos
from electrum_gui.common.wallet.interfaces import ChainModelInterface


class AccountChainModelHandler(ChainModelInterface):
    def generate_unsigned_tx(
        self,
        wallet_id: int,
        coin_code: str,
        to_address: Optional[str] = None,
        value: Optional[int] = None,
        nonce: Optional[int] = None,
        fee_limit: Optional[int] = None,
        fee_price_per_unit: Optional[int] = None,
        payload: Optional[dict] = None,
    ) -> UnsignedTx:
        chain_coin, transfer_coin, fee_coin = coin_manager.get_related_coins(coin_code)
        account = daos.account.query_first_account_by_wallet(wallet_id)

        inputs = []
        outputs = []
        if value is not None:
            inputs.append(
                TransactionInput(address=account.address, value=int(value), token_address=transfer_coin.token_address)
            )
            if to_address is not None:
                to_address = provider_manager.verify_address(chain_coin.code, to_address).normalized_address
                outputs.append(
                    TransactionOutput(address=to_address, value=int(value), token_address=transfer_coin.token_address)
                )

        nonce = int(nonce) if nonce is not None else None  # todo fetch nonce from cache
        fee_limit = int(fee_limit) if fee_limit is not None else None
        fee_price_per_unit = int(fee_price_per_unit) if fee_price_per_unit is not None else None
        payload = dict(payload) if payload is not None else {}
        unsigned_tx = UnsignedTx(
            inputs=inputs,
            outputs=outputs,
            nonce=nonce,
            fee_limit=fee_limit,
            fee_price_per_unit=fee_price_per_unit,
            payload=payload or {},
        )
        extend_if_stc(account, unsigned_tx)
        unsigned_tx = provider_manager.fill_unsigned_tx(chain_coin.code, unsigned_tx)

        return unsigned_tx


def extend_if_stc(account, unsigned_tx):
    chain_info = coin_manager.get_chain_info(account.chain_code)
    if chain_info.chain_affinity != "stc":
        return
    for input_tx in unsigned_tx.inputs:
        input_tx.pubkey = secret_manager.get_verifier(account.pubkey_id).get_pubkey().hex()
