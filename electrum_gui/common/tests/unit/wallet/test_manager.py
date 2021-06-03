from unittest import TestCase
from unittest.mock import patch

from electrum_gui.common.basic.orm import test_utils
from electrum_gui.common.coin.models import CoinModel
from electrum_gui.common.provider.data import Address
from electrum_gui.common.secret.exceptions import InvalidPassword
from electrum_gui.common.secret.models import PubKeyModel, SecretKeyModel
from electrum_gui.common.transaction.models import TxAction
from electrum_gui.common.wallet import manager as wallet_manager
from electrum_gui.common.wallet.models import AccountModel, AssetModel, WalletModel


@test_utils.cls_test_database(WalletModel, AccountModel, AssetModel, PubKeyModel, SecretKeyModel, TxAction, CoinModel)
class TestWalletManager(TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.password = "moon"
        cls.mnemonic = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"
        cls.passphrase = "OneKey"

    def test_import_watchonly_wallet_by_address__eth(self):
        wallet_info = wallet_manager.import_watchonly_wallet_by_address(
            "ETH_WATCHONLY", "eth", "0x8Be73940864fD2B15001536E76b3ECcd85a80a5d"
        )
        self.assertEqual(
            {
                'address': '0x8be73940864fd2b15001536e76b3eccd85a80a5d',
                'address_encoding': None,
                'assets': [
                    {
                        'balance': 0,
                        'coin_code': 'eth',
                        'decimals': 18,
                        'icon': 'https://onekey.243096.com/onekey/images/token/eth/ETH.png',
                        'is_visible': True,
                        'symbol': 'ETH',
                        'token_address': None,
                    }
                ],
                'bip44_path': None,
                'chain_code': 'eth',
                'name': 'ETH_WATCHONLY',
                'wallet_id': 1,
                'wallet_type': 'WATCHONLY',
            },
            wallet_info,
        )

    def test_import_watchonly_wallet_by_address__btc(self):
        self.assertEqual(
            {
                'address': '3Nu7tDXHbqtuMfMi3DMVrnLFabTvaY2FyF',
                'address_encoding': 'P2WPKH-P2SH',
                'assets': [
                    {
                        'balance': 0,
                        'coin_code': 'btc',
                        'decimals': 8,
                        'icon': 'https://onekey.243096.com/onekey/images/token/btc/BTC.png',
                        'is_visible': True,
                        'symbol': 'BTC',
                        'token_address': None,
                    }
                ],
                'bip44_path': None,
                'chain_code': 'btc',
                'name': 'BTC_WATCHONLY',
                'wallet_id': 1,
                'wallet_type': 'WATCHONLY',
            },
            wallet_manager.import_watchonly_wallet_by_address(
                "BTC_WATCHONLY", "btc", "3Nu7tDXHbqtuMfMi3DMVrnLFabTvaY2FyF"
            ),
        )

    def test_import_watchonly_wallet_by_pubkey(self):
        self.assertEqual(
            {
                'address': '0x8be73940864fd2b15001536e76b3eccd85a80a5d',
                'address_encoding': None,
                'assets': [
                    {
                        'balance': 0,
                        'coin_code': 'eth',
                        'decimals': 18,
                        'icon': 'https://onekey.243096.com/onekey/images/token/eth/ETH.png',
                        'is_visible': True,
                        'symbol': 'ETH',
                        'token_address': None,
                    }
                ],
                'bip44_path': None,
                'chain_code': 'eth',
                'name': 'ETH_WATCHONLY_BY_PUBKEY',
                'wallet_id': 1,
                'wallet_type': 'WATCHONLY',
            },
            wallet_manager.import_watchonly_wallet_by_pubkey(
                "ETH_WATCHONLY_BY_PUBKEY",
                "eth",
                bytes.fromhex("02deb60902c06bfed8d78e33337be995d0b3efc28fbc61b6f88cb5cfb27dc4efd1"),
            ),
        )

    def test_import_standalone_wallet_by_prvkey(self):
        self.assertEqual(
            {
                'address': '0x8be73940864fd2b15001536e76b3eccd85a80a5d',
                'address_encoding': None,
                'assets': [
                    {
                        'balance': 0,
                        'coin_code': 'eth',
                        'decimals': 18,
                        'icon': 'https://onekey.243096.com/onekey/images/token/eth/ETH.png',
                        'is_visible': True,
                        'symbol': 'ETH',
                        'token_address': None,
                    }
                ],
                'bip44_path': None,
                'chain_code': 'eth',
                'name': 'ETH_BY_PRVKEY',
                'wallet_id': 1,
                'wallet_type': 'SOFTWARE_STANDALONE_PRVKEY',
            },
            wallet_manager.import_standalone_wallet_by_prvkey(
                "ETH_BY_PRVKEY",
                "eth",
                bytes.fromhex("77f22e0d920c7b59df81a629dc75c27513b5360a45d55f3253454f5d3cb23bab"),
                "moon",
            ),
        )

    def test_import_standalone_wallet_by_mnemonic(self):
        self.assertEqual(
            {
                'address': '0x8be73940864fd2b15001536e76b3eccd85a80a5d',
                'address_encoding': None,
                'assets': [
                    {
                        'balance': 0,
                        'coin_code': 'eth',
                        'decimals': 18,
                        'icon': 'https://onekey.243096.com/onekey/images/token/eth/ETH.png',
                        'is_visible': True,
                        'symbol': 'ETH',
                        'token_address': None,
                    }
                ],
                'bip44_path': "m/44'/60'/0'/0/0",
                'chain_code': 'eth',
                'name': 'ETH_BY_MNEMONIC',
                'wallet_id': 1,
                'wallet_type': 'SOFTWARE_STANDALONE_MNEMONIC',
            },
            wallet_manager.import_standalone_wallet_by_mnemonic(
                "ETH_BY_MNEMONIC",
                "eth",
                self.mnemonic,
                passphrase=self.passphrase,
                password=self.password,
                bip44_path="m/44'/60'/0'/0/0",
            ),
        )

    def test_create_primary_wallets(self):
        self.assertEqual(
            [
                {
                    'address': '3Nu7tDXHbqtuMfMi3DMVrnLFabTvaY2FyF',
                    'address_encoding': 'P2WPKH-P2SH',
                    'assets': [
                        {
                            'balance': 0,
                            'coin_code': 'btc',
                            'decimals': 8,
                            'icon': 'https://onekey.243096.com/onekey/images/token/btc/BTC.png',
                            'is_visible': True,
                            'symbol': 'BTC',
                            'token_address': None,
                        }
                    ],
                    'bip44_path': "m/49'/0'/0'/0/0",
                    'chain_code': 'btc',
                    'name': 'BTC-1',
                    'wallet_id': 1,
                    'wallet_type': 'SOFTWARE_PRIMARY',
                },
                {
                    'address': '0x8be73940864fd2b15001536e76b3eccd85a80a5d',
                    'address_encoding': None,
                    'assets': [
                        {
                            'balance': 0,
                            'coin_code': 'eth',
                            'decimals': 18,
                            'icon': 'https://onekey.243096.com/onekey/images/token/eth/ETH.png',
                            'is_visible': True,
                            'symbol': 'ETH',
                            'token_address': None,
                        }
                    ],
                    'bip44_path': "m/44'/60'/0'/0/0",
                    'chain_code': 'eth',
                    'name': 'ETH-1',
                    'wallet_id': 2,
                    'wallet_type': 'SOFTWARE_PRIMARY',
                },
            ],
            wallet_manager.create_primary_wallets(
                ["btc", "eth"],
                password=self.password,
                mnemonic=self.mnemonic,
                passphrase=self.passphrase,
            ),
        )

    def test_create_next_derived_primary_wallet(self):
        wallet_manager.create_primary_wallets(
            ["eth"],
            password=self.password,
            mnemonic=self.mnemonic,
            passphrase=self.passphrase,
        )

        self.assertEqual(
            {
                'address': '3Nu7tDXHbqtuMfMi3DMVrnLFabTvaY2FyF',
                'address_encoding': 'P2WPKH-P2SH',
                'assets': [
                    {
                        'balance': 0,
                        'coin_code': 'btc',
                        'decimals': 8,
                        'icon': 'https://onekey.243096.com/onekey/images/token/btc/BTC.png',
                        'is_visible': True,
                        'symbol': 'BTC',
                        'token_address': None,
                    }
                ],
                'bip44_path': "m/49'/0'/0'/0/0",
                'chain_code': 'btc',
                'name': 'BTC-1',
                'wallet_id': 2,
                'wallet_type': 'SOFTWARE_PRIMARY',
            },
            wallet_manager.create_next_derived_primary_wallet("btc", "BTC-1", "moon"),
        )

        self.assertEqual(
            {
                'address': '0xd927952eed3a0a838bbe2db0ba5a15673003903d',
                'address_encoding': None,
                'assets': [
                    {
                        'balance': 0,
                        'coin_code': 'eth',
                        'decimals': 18,
                        'icon': 'https://onekey.243096.com/onekey/images/token/eth/ETH.png',
                        'is_visible': True,
                        'symbol': 'ETH',
                        'token_address': None,
                    }
                ],
                'bip44_path': "m/44'/60'/0'/0/1",
                'chain_code': 'eth',
                'name': 'ETH-2',
                'wallet_id': 3,
                'wallet_type': 'SOFTWARE_PRIMARY',
            },
            wallet_manager.create_next_derived_primary_wallet("eth", "ETH-2", "moon"),
        )

        self.assertEqual(
            {
                'address': '34y7g9uRnjJwvu2zLJVMfrucnbzgyYc4af',
                'address_encoding': 'P2WPKH-P2SH',
                'assets': [
                    {
                        'balance': 0,
                        'coin_code': 'btc',
                        'decimals': 8,
                        'icon': 'https://onekey.243096.com/onekey/images/token/btc/BTC.png',
                        'is_visible': True,
                        'symbol': 'BTC',
                        'token_address': None,
                    }
                ],
                'bip44_path': "m/49'/0'/1'/0/0",
                'chain_code': 'btc',
                'name': 'BTC-2',
                'wallet_id': 4,
                'wallet_type': 'SOFTWARE_PRIMARY',
            },
            wallet_manager.create_next_derived_primary_wallet("btc", "BTC-2", "moon"),
        )

    def test_export_mnemonic__primary_wallet(self):
        wallet_info = wallet_manager.create_primary_wallets(
            ["eth"],
            password=self.password,
            mnemonic=self.mnemonic,
            passphrase=self.passphrase,
        )[0]
        self.assertEqual(
            (self.mnemonic, self.passphrase),
            wallet_manager.export_mnemonic(wallet_info["wallet_id"], self.password),
        )

    def test_export_mnemonic__standalone_mnemonic_wallet(self):
        wallet_info = wallet_manager.import_standalone_wallet_by_mnemonic(
            "ETH-1",
            "eth",
            password=self.password,
            mnemonic=self.mnemonic,
            passphrase=self.passphrase,
        )

        self.assertEqual(
            (self.mnemonic, self.passphrase), wallet_manager.export_mnemonic(wallet_info["wallet_id"], self.password)
        )

    @patch("electrum_gui.common.wallet.manager.provider_manager.get_address")
    def test_search_existing_wallets(self, fake_get_address):
        fake_get_address.side_effect = (
            lambda chain_code, address: Address(address=address, balance=18888, existing=True)
            if address == "0xa0331fcfa308e488833de1fe16370b529fa7c720"
            else Address(address=address, balance=0, existing=False)
        )

        self.assertEqual(
            [
                {
                    'address': '3Nu7tDXHbqtuMfMi3DMVrnLFabTvaY2FyF',
                    'address_encoding': 'P2WPKH-P2SH',
                    'balance': 0,
                    'bip44_path': "m/49'/0'/0'/0/0",
                    'chain_code': 'btc',
                    'name': 'BTC-1',
                },
                {
                    'address': '0xa0331fcfa308e488833de1fe16370b529fa7c720',
                    'address_encoding': None,
                    'balance': 18888,
                    'bip44_path': "m/44'/60'/0'/0/11",
                    'chain_code': 'eth',
                    'name': 'ETH-1',
                },
            ],
            wallet_manager.search_existing_wallets(["btc", "eth"], self.mnemonic, passphrase=self.passphrase),
        )

    def test_update_wallet_password(self):
        wallet_info = wallet_manager.import_standalone_wallet_by_mnemonic(
            "ETH-1",
            "eth",
            password=self.password,
            mnemonic=self.mnemonic,
            passphrase=self.passphrase,
        )

        with self.assertRaises(InvalidPassword):
            wallet_manager.update_wallet_password(wallet_info["wallet_id"], "hello world", "bye")

        wallet_manager.update_wallet_password(wallet_info["wallet_id"], self.password, "bye")
        wallet_manager.update_wallet_password(wallet_info["wallet_id"], "bye", self.password)
