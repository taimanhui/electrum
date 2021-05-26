from enum import IntEnum, unique


@unique
class WalletType(IntEnum):
    WATCHONLY = 10
    SOFTWARE_PRIMARY = 21
    SOFTWARE_STANDALONE_MNEMONIC = 22
    SOFTWARE_STANDALONE_PRVKEY = 23
    HARDWARE = 30

    @classmethod
    def to_choices(cls):
        return (
            (cls.WATCHONLY, "Watchonly Wallet"),
            (cls.SOFTWARE_PRIMARY, "Primary Software Wallet"),
            (cls.SOFTWARE_STANDALONE_MNEMONIC, "Standalone Software Wallet From Mnemonic"),
            (cls.SOFTWARE_STANDALONE_PRVKEY, "Standalone Software Wallet From PrivateKey"),
            (cls.HARDWARE, "Hardware"),
        )
