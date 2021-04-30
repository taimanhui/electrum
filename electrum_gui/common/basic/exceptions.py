import json
from enum import IntEnum, unique


@unique
class ResultStatus(IntEnum):
    SUCCESS = 0
    FAILED = 1
    APP_USED = 2


class OneKeyException(Exception):
    key = "msg_unknown_error"
    status = ResultStatus.FAILED.value


class UnavailablePrivateKey(OneKeyException):
    key = "msg_incorrect_private_key"


class InvalidKeystoreFormat(OneKeyException):
    key = "msg_incorrect_keystore_format"


class InvalidMnemonicFormat(OneKeyException):
    key = "msg_incorrect_recovery_phrase_format"


class UnavailableBtcAddr(OneKeyException):
    key = "msg_incorrect_bitcoin_address"


class InvalidPassword(OneKeyException):
    key = "msg_incorrect_password"


class UnavailablePublicKey(OneKeyException):
    key = "msg_incorrect_public_key"


class UnavailableEthAddr(OneKeyException):
    key = "msg_incorrect_eth_address"


def catch_exception(func):
    def wrapper(*args, **kwargs):
        def _filter_params(err=None):
            error_msg = {"fun_name": func.__name__}
            error_msg.update({k: v for k, v in kwargs.items() if k not in ["seed", "password", "mnemonic"]})
            if err is not None:
                error_msg.update({"err_msg_detail": "%s:%s" % (err.__class__.__name__, err.args)})
            return error_msg

        try:
            result = func(*args, **kwargs)
            out = {"status": ResultStatus.SUCCESS.value, "info": result}
        except OneKeyException as e:
            out = {"status": e.status, "err_msg_key": e.key}
        except Exception as e:
            out = {
                "status": OneKeyException.status,
                "err_msg_key": OneKeyException.key,
                "low_level_error": _filter_params(e),
            }
        return json.dumps(out)

    return wrapper
