class OneKeyException(Exception):
    key = "msg__unknown_error"


class UnavailablePrivateKey(OneKeyException):
    key = "msg__incorrect_private_key"


class InvalidKeystoreFormat(OneKeyException):
    key = "msg__incorrect_keystore_format"


class InvalidMnemonicFormat(OneKeyException):
    key = "msg__incorrect_recovery_phrase_format"


class UnavailableBtcAddr(OneKeyException):
    key = "msg__incorrect_bitcoin_address"


class InvalidPassword(OneKeyException):
    key = "msg__incorrect_password"


class UnavailablePublicKey(OneKeyException):
    key = "msg__incorrect_public_key"


class UnavailableEthAddr(OneKeyException):
    key = "msg__incorrect_eth_address"


class IncorrectAddress(OneKeyException):
    key = "msg__incorrect_address"


class InactiveAddress(OneKeyException):
    key = "msg__the_address_has_not_been_activated_please_enter_receipt_identifier"
