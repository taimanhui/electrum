import datetime
from typing import List, Optional

from electrum_gui.common.wallet.data import WalletType
from electrum_gui.common.wallet.models import WalletModel


def create_wallet(
    name: str,
    wallet_type: WalletType,
    chain_code: str,
) -> WalletModel:
    return WalletModel.create(
        name=name,
        type=wallet_type,
        chain_code=chain_code,
    )


def list_all_wallets(chain_code: str = None, wallet_type: WalletType = None) -> List[WalletModel]:
    models = WalletModel.select()

    if chain_code is not None:
        models = models.where(WalletModel.chain_code == chain_code)

    if wallet_type is not None:
        models = models.where(WalletModel.type == wallet_type)

    return list(models)


def get_wallet_by_id(wallet_id: int) -> Optional[WalletModel]:
    return WalletModel.get_or_none(WalletModel.id == wallet_id)


def has_primary_wallet() -> bool:
    return WalletModel.select().where(WalletModel.type == WalletType.SOFTWARE_PRIMARY).count() > 0


def get_first_primary_wallet() -> Optional[WalletModel]:
    return WalletModel.get_or_none(WalletModel.type == WalletType.SOFTWARE_PRIMARY)


def update_wallet_name(wallet_id: int, name: str):
    WalletModel.update(name=name, modified_time=datetime.datetime.now()).where(WalletModel.id == wallet_id).execute()


def delete_wallet_by_id(wallet_id: int):
    WalletModel.delete_by_id(wallet_id)
