from dataclasses import dataclass
from enum import IntEnum, unique
from typing import Optional

from electrum_gui.common.basic.dataclass.dataclass import DataClassMixin


@unique
class ChainModel(IntEnum):
    UTXO = 10
    ACCOUNT = 20


@dataclass
class ChainInfo(DataClassMixin):
    chain_code: str  # unique chain coin
    fee_code: str  # which coin is used to provide fee (omni chain uses btc, neo uses neo_gas etc.)
    name: str  # full name of chain
    chain_model: ChainModel  # model of chain (UTXO, Account etc.)
    qr_code_prefix: str  # QR code prefix of address
    chain_id: Optional[str] = None  # optional, identify multi forked chains by chain_id (use by eth etc.)
    chain_affinity: Optional[str] = None  # optional, used by testnet or fork chain


@dataclass
class CoinInfo(DataClassMixin):
    code: str  # unique code
    chain_code: str  # which chain does it belong to

    name: str  # full name of coin
    symbol: str  # symbol of coin

    decimals: int  # decimals of coin
    icon: Optional[str] = None  # icon url of coin

    token_address: Optional[str] = None  # optional, used by tokens
