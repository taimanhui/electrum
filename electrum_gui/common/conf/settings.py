from os import path

from electrum_gui.common.coin import codes
from electrum_gui.common.conf.utils import get_data_dir

try:
    from electrum import constants

    IS_DEV = constants.net.TESTNET
except ImportError:
    IS_DEV = False

PROJECT_DIR = path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))))
DATA_DIR = get_data_dir()

LOGGING = {
    "version": 1,
    "disable_existing_loggers": False,
    "handlers": {
        'console': {
            'class': 'logging.StreamHandler',
            'level': 'DEBUG',
        },
    },
    "loggers": {
        "app": {
            'handlers': ['console'],
            'level': 'INFO',
            "propagate": False,
        }
    },
}

DATABASE = {
    "default": {
        "name": f"{DATA_DIR}/database.sqlite",
    },
}

DB_MODULES = [
    "electrum_gui.common.coin",
    "electrum_gui.common.price",
    "electrum_gui.common.transaction",
    "electrum_gui.common.secret",
    "electrum_gui.common.wallet",
]

ENABLED_CHAIN_COINS = (
    [
        codes.TBTC,
        codes.TETH,
        codes.TBSC,
        codes.THECO,
        codes.TOKT,
    ]
    if IS_DEV
    else [
        codes.BTC,
        codes.ETH,
        codes.BSC,
        codes.HECO,
        codes.OKT,
    ]
)


PRICING_COIN_MAPPING = {
    codes.TBTC: codes.BTC,
    codes.TETH: codes.ETH,
    codes.TBSC: codes.BSC,
    codes.THECO: codes.HECO,
    codes.TOKT: codes.OKT,
}  # Map the price of the mainnet coin to the testnet coin

COINGECKO_API_HOST = "https://api.coingecko.com"
COINGECKO_IDS = {
    codes.ETH: "ethereum",
    codes.BSC: "binancecoin",
    codes.HECO: "huobi-token",
    codes.OKT: "okexchain",
}  # Map the coin code to coingecko id

# loading local_settings.py on project root
try:
    from local_settings import *  # noqa
except ImportError:
    pass
