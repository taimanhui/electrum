# Copyright (c) The Diem Core Contributors
# SPDX-License-Identifier: Apache-2.0
# Copyright (c) The starcoin Core Contributors
"""Provides LocalAccount class for holding local account private key.

LocalAccount provides operations we need for creating auth key, account address and signing
raw transaction.
"""

from nacl.public import PrivateKey, PublicKey
from nacl.signing import SigningKey

from electrum_gui.common.provider.chains.stc.sdk import starcoin_types
from electrum_gui.common.provider.chains.stc.sdk.utils import utils
from electrum_gui.common.provider.chains.stc.sdk.utils.auth_key import AuthKey


class LocalAccount:
    """LocalAccount is like a wallet account

    WARN: This is handy class for creating tests for your application, but may not ideal for your
    production code, because it uses a specific implementaion of ed25519 and requires loading your
    private key into memory and hand over to code from external.
    You should always choose more secure way to handle your private key
    (e.g. https://en.wikipedia.org/wiki/Hardware_security_module) in production and do not give
    your private key to any code from external if possible.
    """

    @staticmethod
    def generate() -> "LocalAccount":
        """Generate a random private key and initialize local account"""

        private_key = SigningKey.generate()
        return LocalAccount(private_key)

    private_key: PrivateKey

    def __init__(self, private_key: PrivateKey) -> None:
        self.private_key = private_key

    @property
    def auth_key(self) -> AuthKey:
        return AuthKey.from_public_key(self.public_key)

    @property
    def account_address(self) -> starcoin_types.AccountAddress:
        return self.auth_key.account_address()

    @property
    def public_key_bytes(self) -> bytes:
        return utils.public_key_bytes(self.public_key)

    @property
    def public_key(self) -> PublicKey:
        return self.private_key.public_key()

    def sign(self, txn: starcoin_types.RawTransaction) -> starcoin_types.SignedUserTransaction:
        """Create signed transaction for given raw transaction"""
        signature = SigningKey(self.private_key[:32]).sign(utils.raw_transaction_signing_msg(txn))
        return utils.create_signed_transaction(txn, self.public_key_bytes, signature)
