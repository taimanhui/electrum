import electrum.crypto
import electrum.util
from electrum_gui.common.basic.functional.require import require
from electrum_gui.common.secret import exceptions


def encrypt_data(password: str, data: str) -> str:
    require(bool(password))
    return electrum.crypto.pw_encode(data, password, version=1)


def decrypt_data(password: str, data: str) -> str:
    require(bool(password))
    try:
        return electrum.crypto.pw_decode(data, password, version=1)
    except electrum.util.InvalidPassword as e:
        raise exceptions.InvalidPassword(str(e))
