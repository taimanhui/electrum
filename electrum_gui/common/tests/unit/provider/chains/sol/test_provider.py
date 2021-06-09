from unittest import TestCase
from unittest.mock import Mock

import base58

from electrum_gui.common.provider.chains.sol import SOLProvider
from electrum_gui.common.provider.data import (
    AddressValidation,
    EstimatedTimeOnPrice,
    PricesPerUnit,
    SignedTx,
    TransactionInput,
    TransactionOutput,
    UnsignedTx,
)


class TestSOLProvider(TestCase):
    def setUp(self) -> None:
        self.fake_chain_info = Mock()
        self.fake_coins_loader = Mock()
        self.fake_client_selector = Mock()
        self.provider = SOLProvider(
            chain_info=self.fake_chain_info,
            coins_loader=self.fake_coins_loader,
            client_selector=self.fake_client_selector,
        )

    def test_verify_address(self):
        """
        system account/ mint address is on curve , associated token account fall off curve
        """
        # system account
        self.assertEqual(
            AddressValidation(
                normalized_address="44wY1yr8668sCV1EivZTGd3wT6JoNpfFanGQ2YhJTvup",
                display_address="44wY1yr8668sCV1EivZTGd3wT6JoNpfFanGQ2YhJTvup",
                is_valid=True,
            ),
            self.provider.verify_address("44wY1yr8668sCV1EivZTGd3wT6JoNpfFanGQ2YhJTvup"),
        )
        # associated token account
        self.assertEqual(
            AddressValidation(
                normalized_address="2GVWvkDH6kbEjarScotT8zthQ9vZR6CJ5Qx7di4aLBSL",
                display_address="2GVWvkDH6kbEjarScotT8zthQ9vZR6CJ5Qx7di4aLBSL",
                is_valid=True,
            ),
            self.provider.verify_address("2GVWvkDH6kbEjarScotT8zthQ9vZR6CJ5Qx7di4aLBSL"),
        )
        # invalid key length
        self.assertEqual(
            AddressValidation(
                normalized_address="44wY1yr8668sCV1EivZTGd3wT6JoNpfFanGQ2YhJTvupxx",
                display_address="44wY1yr8668sCV1EivZTGd3wT6JoNpfFanGQ2YhJTvupxx",
                is_valid=False,
            ),
            self.provider.verify_address("44wY1yr8668sCV1EivZTGd3wT6JoNpfFanGQ2YhJTvupxx"),
        )
        # mint address
        self.assertEqual(
            AddressValidation(
                normalized_address="DF55N1ZCBfEaQAc8PstaQ657sFmGjDwvNDoGQucuqbrD",
                display_address="DF55N1ZCBfEaQAc8PstaQ657sFmGjDwvNDoGQucuqbrD",
                is_valid=True,
            ),
            self.provider.verify_address("DF55N1ZCBfEaQAc8PstaQ657sFmGjDwvNDoGQucuqbrD"),
        )

    def test_pubkey_to_address(self):
        verifier = Mock(get_pubkey=Mock(return_value=base58.b58decode("44wY1yr8668sCV1EivZTGd3wT6JoNpfFanGQ2YhJTvup")))
        self.assertEqual(
            "44wY1yr8668sCV1EivZTGd3wT6JoNpfFanGQ2YhJTvup", self.provider.pubkey_to_address(verifier=verifier)
        )
        verifier.get_pubkey.assert_called_once()

    def test_fill_unsigned_tx(self):
        sender = "44wY1yr8668sCV1EivZTGd3wT6JoNpfFanGQ2YhJTvup"
        receiver = "EP5Bg4hKT2K3KCpko6RJEYAW49Z7NsfbenVd5mhj8h2q"
        mint = "DF55N1ZCBfEaQAc8PstaQ657sFmGjDwvNDoGQucuqbrD"
        funded_token_account = "3Da3sHbJBBNkTmdCkK7XzWpU32UPDWkhja1ixFMGp1AZ"
        not_funded_token_account = "8xhcM8bCcnJdsKjnBzRmdjVbeHnNeSPNYBaFs2DhUTjH"
        funded_token_account_info = {
            "data": {
                "parsed": {
                    "info": {
                        "mint": "DF55N1ZCBfEaQAc8PstaQ657sFmGjDwvNDoGQucuqbrD",
                        "owner": "EP5Bg4hKT2K3KCpko6RJEYAW49Z7NsfbenVd5mhj8h2q",
                    },
                },
            },
            "lamports": 2039280,
            "owner": "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA",
        }
        funded_system_account_info = {
            "data": ["", "base64"],
            "executable": False,
            "lamports": 1000000000,
            "owner": "11111111111111111111111111111111",
            "rentEpoch": 135,
        }
        fake_client = Mock(
            get_prices_per_unit_of_fee=Mock(
                return_value=PricesPerUnit(
                    slow=EstimatedTimeOnPrice(price=5000),
                    normal=EstimatedTimeOnPrice(price=5000),
                    fast=EstimatedTimeOnPrice(price=5000),
                )
            ),
            get_account_info=Mock(
                side_effect=[funded_system_account_info, funded_token_account_info, funded_token_account_info, None]
            ),
            get_fees=Mock(return_value=(5000, "7Zf5mBZPkYktFZ9AGPzkqkLbtcwVCoLeDFQmZvHgEuz1")),
        )

        self.fake_client_selector.return_value = fake_client

        with self.subTest("Empty UnsignedTx"):
            self.assertEqual(
                UnsignedTx(fee_limit=1, fee_price_per_unit=5000),
                self.provider.fill_unsigned_tx(
                    UnsignedTx(),
                ),
            )
            fake_client.get_prices_per_unit_of_fee.assert_called_once()
            fake_client.get_account_info.assert_not_called()
            fake_client.reset_mock()

        with self.subTest("Transfer SOL"):
            self.assertEqual(
                UnsignedTx(
                    inputs=[TransactionInput(address=sender, value=213)],
                    outputs=[TransactionOutput(address=receiver, value=213)],
                    fee_price_per_unit=5000,
                    fee_limit=1,
                    payload={"recent_blockhash": "7Zf5mBZPkYktFZ9AGPzkqkLbtcwVCoLeDFQmZvHgEuz1"},
                ),
                self.provider.fill_unsigned_tx(
                    UnsignedTx(
                        inputs=[TransactionInput(address=sender, value=213)],
                        outputs=[TransactionOutput(address=receiver, value=213)],
                        fee_price_per_unit=5000,
                        fee_limit=1,
                    )
                ),
            )
            fake_client.get_prices_per_unit_of_fee.assert_not_called()
            fake_client.get_account_info.assert_not_called()
            fake_client.get_fees.assert_called_once()
            fake_client.reset_mock()

        with self.subTest("Transfer spl-token with a system account receiver"):
            self.assertEqual(
                UnsignedTx(
                    inputs=[TransactionInput(address=sender, value=213)],
                    outputs=[TransactionOutput(address=receiver, value=213, token_address=mint)],
                    fee_price_per_unit=5000,
                    fee_limit=1,
                    payload={
                        "account_funded": True,
                        "is_token_account": False,
                        "recent_blockhash": "7Zf5mBZPkYktFZ9AGPzkqkLbtcwVCoLeDFQmZvHgEuz1",
                    },
                ),
                self.provider.fill_unsigned_tx(
                    UnsignedTx(
                        inputs=[TransactionInput(address=sender, value=213)],
                        outputs=[TransactionOutput(address=receiver, value=213, token_address=mint)],
                        fee_price_per_unit=5000,
                        fee_limit=1,
                    )
                ),
            )
            self.assertEqual(2, fake_client.get_account_info.call_count)
            fake_client.get_prices_per_unit_of_fee.assert_not_called()
            fake_client.get_fees.assert_called_once()
            fake_client.get_fees.reset_mock()
        with self.subTest("Transfer spl-token with a funded associated_token_account receiver"):
            self.assertEqual(
                UnsignedTx(
                    inputs=[TransactionInput(address=sender, value=213)],
                    outputs=[TransactionOutput(address=funded_token_account, value=213, token_address=mint)],
                    fee_price_per_unit=5000,
                    fee_limit=1,
                    payload={
                        "account_funded": True,
                        "is_token_account": True,
                        "recent_blockhash": "7Zf5mBZPkYktFZ9AGPzkqkLbtcwVCoLeDFQmZvHgEuz1",
                    },
                ),
                self.provider.fill_unsigned_tx(
                    UnsignedTx(
                        inputs=[TransactionInput(address=sender, value=213)],
                        outputs=[TransactionOutput(address=funded_token_account, value=213, token_address=mint)],
                        fee_price_per_unit=5000,
                        fee_limit=1,
                    )
                ),
            )
            self.assertEqual(3, fake_client.get_account_info.call_count)
            fake_client.get_prices_per_unit_of_fee.assert_not_called()
            fake_client.get_fees.assert_called_once()
            fake_client.get_fees.reset_mock()
        with self.subTest("Transfer spl-token with a not funded associated_token_account receiver"):
            with self.assertRaises(AssertionError):
                self.provider.fill_unsigned_tx(
                    UnsignedTx(
                        inputs=[TransactionInput(address=sender, value=213)],
                        outputs=[TransactionOutput(address=not_funded_token_account, value=213, token_address=mint)],
                        fee_price_per_unit=5000,
                        fee_limit=1,
                    )
                )
            self.assertEqual(4, fake_client.get_account_info.call_count)
            fake_client.get_prices_per_unit_of_fee.assert_not_called()
            fake_client.get_fees.assert_not_called()
            fake_client.reset_mock()

    def test_sign_transaction(self):
        sender = "44wY1yr8668sCV1EivZTGd3wT6JoNpfFanGQ2YhJTvup"
        receiver = "EP5Bg4hKT2K3KCpko6RJEYAW49Z7NsfbenVd5mhj8h2q"
        funded_token_account = "3Da3sHbJBBNkTmdCkK7XzWpU32UPDWkhja1ixFMGp1AZ"
        mint = "DF55N1ZCBfEaQAc8PstaQ657sFmGjDwvNDoGQucuqbrD"
        with self.subTest("test sign SOL transaction"):
            singers = {
                "44wY1yr8668sCV1EivZTGd3wT6JoNpfFanGQ2YhJTvup": Mock(
                    sign=Mock(
                        return_value=(
                            bytes.fromhex(
                                "53efc52376eb72b5acc5610d0f187657cf2c4f946e892ba0abe6a85f1e676e52e932d72cc2eb63fc67f6c7b22b19779d1fc2df56c7b4e073c17e5e2955b55703"
                            ),
                            0,
                        )
                    )
                )
            }
            self.assertEqual(
                SignedTx(
                    raw_tx="AVPvxSN263K1rMVhDQ8YdlfPLE+UbokroKvmqF8eZ25S6TLXLMLrY/xn9seyKxl3nR"
                    "/C31bHtOBzwX5eKVW1VwMBAAEDLZV3Q2JycMZVXXGqdkJMdhMti7TKsmkRiMp1aQ450MvGz3BQ"
                    "+dcjipDegGm2Ygl+Tkl+pgM43tuiff4ZVqpfwgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAYYPEWyA0ySW/tlaqZ9I8DE7BlYBx2ETr3gO8HJjVCPIBAgIAAQwCAAAA1QAAAAAAAAA=",
                    txid="2gLLT5ysgK9iR8JMunKTzENnyswRj2oscdn2AaM1EgDXMcMY9sJPvYax8Prw2isEzgAq75dVKoM1n4x2PCrxz35U",
                ),
                self.provider.sign_transaction(
                    UnsignedTx(
                        inputs=[TransactionInput(address=sender, value=213)],
                        outputs=[TransactionOutput(address=receiver, value=213)],
                        fee_price_per_unit=5000,
                        fee_limit=1,
                        payload={"recent_blockhash": "7Zf5mBZPkYktFZ9AGPzkqkLbtcwVCoLeDFQmZvHgEuz1"},
                    ),
                    singers,
                ),
            )

        with self.subTest("test sign spl-token transfer with associated token account funded as receiver"):
            singers = {
                "44wY1yr8668sCV1EivZTGd3wT6JoNpfFanGQ2YhJTvup": Mock(
                    sign=Mock(
                        return_value=(
                            bytes.fromhex(
                                "ccf09071446b86d5170c1950c4dd5b9af202cf369f04081aca069d04b34a57e151030d50a9939090ba16bb1a10985c5e4b823e1764ec31a51986f66f20a4a707"
                            ),
                            0,
                        )
                    )
                )
            }
            self.assertEqual(
                SignedTx(
                    raw_tx="AczwkHFEa4bVFwwZUMTdW5ryAs82nwQIGsoGnQSzSlfhUQMNUKmTkJC6FrsaEJhcXkuCPhdk7DGlGYb2byCkpwcBAAEELZV3Q2JycMZVXXGqdkJMdhMti7TKsmkRiMp1aQ450MsS06Rl51ljPeD8QjLaR2J7BCe/9QV5x4F0m7X4NnKsTSDvtnGzSsXjHflAjG1ZH6Qzwax6i5mYK9txKwZM2RnyBt324ddloZPZy+FGzut5rBy0he1fWzeROoz1hX7/AKlhg8RbIDTJJb+2Vqpn0jwMTsGVgHHYROveA7wcmNUI8gEDBAECAAAJA9UAAAAAAAAA",
                    txid="56ef87qzWfqgCQEuh4MChgwgc543Xh2viR3H4mE85BE1KqTGTd3MkzUtoh18gQS1L8kEtmELyLAEuKb4c3xg36F8",
                ),
                self.provider.sign_transaction(
                    UnsignedTx(
                        inputs=[TransactionInput(address=sender, value=213, token_address=mint)],
                        outputs=[TransactionOutput(address=funded_token_account, value=213, token_address=mint)],
                        fee_price_per_unit=5000,
                        fee_limit=1,
                        payload={
                            "recent_blockhash": "7Zf5mBZPkYktFZ9AGPzkqkLbtcwVCoLeDFQmZvHgEuz1",
                            "account_funded": True,
                            "is_token_account": True,
                        },
                    ),
                    singers,
                ),
            )

        with self.subTest(
            "test sign spl-token transfer with system account as receiver and associated token account funded"
        ):
            singers = {
                "44wY1yr8668sCV1EivZTGd3wT6JoNpfFanGQ2YhJTvup": Mock(
                    sign=Mock(
                        return_value=(
                            bytes.fromhex(
                                "ccf09071446b86d5170c1950c4dd5b9af202cf369f04081aca069d04b34a57e151030d50a9939090ba16bb1a10985c5e4b823e1764ec31a51986f66f20a4a707"
                            ),
                            0,
                        )
                    )
                )
            }
            self.assertEqual(
                SignedTx(
                    raw_tx="AczwkHFEa4bVFwwZUMTdW5ryAs82nwQIGsoGnQSzSlfhUQMNUKmTkJC6FrsaEJhcXkuCPhdk7DGlGYb2byCkpwcBAAEELZV3Q2JycMZVXXGqdkJMdhMti7TKsmkRiMp1aQ450MsS06Rl51ljPeD8QjLaR2J7BCe/9QV5x4F0m7X4NnKsTSDvtnGzSsXjHflAjG1ZH6Qzwax6i5mYK9txKwZM2RnyBt324ddloZPZy+FGzut5rBy0he1fWzeROoz1hX7/AKlhg8RbIDTJJb+2Vqpn0jwMTsGVgHHYROveA7wcmNUI8gEDBAECAAAJA9UAAAAAAAAA",
                    txid="56ef87qzWfqgCQEuh4MChgwgc543Xh2viR3H4mE85BE1KqTGTd3MkzUtoh18gQS1L8kEtmELyLAEuKb4c3xg36F8",
                ),
                self.provider.sign_transaction(
                    UnsignedTx(
                        inputs=[TransactionInput(address=sender, value=213, token_address=mint)],
                        outputs=[TransactionOutput(address=receiver, value=213, token_address=mint)],
                        fee_price_per_unit=5000,
                        fee_limit=1,
                        payload={
                            "recent_blockhash": "7Zf5mBZPkYktFZ9AGPzkqkLbtcwVCoLeDFQmZvHgEuz1",
                            "account_funded": True,
                            "is_token_account": False,
                        },
                    ),
                    singers,
                ),
            )

        with self.subTest(
            "test sign spl-token transfer with system account as receiver and associated token account not funded"
        ):
            singers = {
                "44wY1yr8668sCV1EivZTGd3wT6JoNpfFanGQ2YhJTvup": Mock(
                    sign=Mock(
                        return_value=(
                            bytes.fromhex(
                                "b55639d9a2e6c98085d40220bb0543135303de30dbc5270f8f2cf77f77960f566aa13007dafa846ebb6f4a4f4d853d137186e52a745b494341a4161ab9170207"
                            ),
                            0,
                        )
                    )
                )
            }
            self.assertEqual(
                SignedTx(
                    raw_tx="AbVWOdmi5smAhdQCILsFQxNTA94w28UnD48s9393lg9WaqEwB9r6hG67b0pPTYU9E3GG5Sp0W0lDQaQWGrkXAgcBAAYJLZV3Q2JycMZVXXGqdkJMdhMti7TKsmkRiMp1aQ450Msg77Zxs0rF4x35QIxtWR+kM8GseouZmCvbcSsGTNkZ8hLTpGXnWWM94PxCMtpHYnsEJ7/1BXnHgXSbtfg2cqxNxs9wUPnXI4qQ3oBptmIJfk5JfqYDON7bon3+GVaqX8K15vt38fcbdjbggfEn43Oo0AWwkjr5k2l5HfiowlunDgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABt324ddloZPZy+FGzut5rBy0he1fWzeROoz1hX7/AKkGp9UXGSxcUSGMyUw9SvF/WNruCJuh/UTj29mKAAAAAIyXJY9OJInxuz0QKRSODYMLWhOZ2v8QhASOe9jb6fhZYYPEWyA0ySW/tlaqZ9I8DE7BlYBx2ETr3gO8HJjVCPICCAcAAQMEBQYHAAYEAgEAAAkD1QAAAAAAAAA=",
                    txid="4dHCJpb8gzLLwxHzQ7j1X8ZMkxxo3vNexneMYnzDHtAdfB1eaNysJz84AzyctfYqnrANgoSLLQegDiyb4PTvrX3c",
                ),
                self.provider.sign_transaction(
                    UnsignedTx(
                        inputs=[TransactionInput(address=sender, value=213, token_address=mint)],
                        outputs=[TransactionOutput(address=receiver, value=213, token_address=mint)],
                        fee_price_per_unit=5000,
                        fee_limit=1,
                        payload={
                            "recent_blockhash": "7Zf5mBZPkYktFZ9AGPzkqkLbtcwVCoLeDFQmZvHgEuz1",
                            "account_funded": False,
                            "is_token_account": False,
                        },
                    ),
                    singers,
                ),
            )
