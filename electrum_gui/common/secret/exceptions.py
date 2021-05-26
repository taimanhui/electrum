import electrum.util


class InvalidECPointException(Exception):
    pass


class InvalidPassword(electrum.util.InvalidPassword):
    pass
