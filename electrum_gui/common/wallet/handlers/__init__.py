from electrum_gui.common.coin.data import ChainModel
from electrum_gui.common.wallet.handlers.account import AccountChainModelHandler

_REGISTER = {
    ChainModel.ACCOUNT: AccountChainModelHandler,
}
_CACHE = {}


def get_handler_by_chain_model(chain_model: ChainModel):
    if chain_model not in _REGISTER:
        raise NotImplementedError()

    handler = _CACHE.get(chain_model)
    if handler is None:
        handler = _REGISTER[chain_model]()
        _CACHE[chain_model] = handler

    return handler
