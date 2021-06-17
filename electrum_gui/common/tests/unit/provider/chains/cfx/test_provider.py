from unittest import TestCase
from unittest.mock import Mock

from electrum_gui.common.provider.chains.cfx import CFXProvider
from electrum_gui.common.provider.data import (
    AddressValidation,
    EstimatedTimeOnPrice,
    PricesPerUnit,
    SignedTx,
    TransactionInput,
    TransactionOutput,
    UnsignedTx,
)


class TestCFXProvider(TestCase):
    def setUp(self) -> None:
        self.fake_chain_info = Mock(chain_id="1")
        self.fake_coins_loader = Mock()
        self.fake_client_selector = Mock()
        self.provider = CFXProvider(
            chain_info=self.fake_chain_info,
            coins_loader=self.fake_coins_loader,
            client_selector=self.fake_client_selector,
        )

    def test_verify_address(self):
        self.assertEqual(
            AddressValidation(
                normalized_address="cfxtest:aamt7ycdybdb5j307yw5t5ejx8d854ykjjhv7n8h6v",
                display_address="cfxtest:aamt7ycdybdb5j307yw5t5ejx8d854ykjjhv7n8h6v",
                is_valid=True,
            ),
            self.provider.verify_address("cfxtest:aamt7ycdybdb5j307yw5t5ejx8d854ykjjhv7n8h6v"),
        )
        self.assertEqual(
            AddressValidation(
                normalized_address="cfx:aaknd2naz3pwkgrxdjy5nxp2p352bpdg42bgmwnwjt",
                display_address="cfx:aaknd2naz3pwkgrxdjy5nxp2p352bpdg42bgmwnwjt",
                is_valid=True,
            ),
            self.provider.verify_address("cfx:aaknd2naz3pwkgrxdjy5nxp2p352bpdg42bgmwnwjt"),
        )
        self.assertEqual(
            AddressValidation(normalized_address="", display_address="", is_valid=False),
            self.provider.verify_address(""),
        )
        self.assertEqual(
            AddressValidation(normalized_address="", display_address="", is_valid=False),
            self.provider.verify_address("0x"),
        )

    def test_pubkey_to_address(self):
        verifier = Mock(get_pubkey=Mock(return_value=b"\4" + b"\0" * 64))
        self.assertEqual(
            "cfxtest:aatvt6p0fp5skerxbkaneuc2khw3tsw90y11js9sgj", self.provider.pubkey_to_address(verifier=verifier)
        )
        verifier.get_pubkey.assert_called_once_with(compressed=False)

    def test_fill_unsigned_tx(self):
        external_address_a = "cfxtest:aakrn3d7hezu0tcafpe92bbbsy1fw2u5za1xa2uvgp"
        external_address_b = "cfxtest:aaknd2naz3pwkgrxdjy5nxp2p352bpdg42nr3cr2pf"
        contract_address = "cfxtest:acepe88unk7fvs18436178up33hb4zkuf62a9dk1gv"

        fake_client = Mock(
            get_prices_per_unit_of_fee=Mock(return_value=PricesPerUnit(normal=EstimatedTimeOnPrice(price=1))),
            get_address=Mock(return_value=Mock(nonce=10)),
            estimate_gas_and_collateral=Mock(return_value=(21000, 0)),
            get_epoch_number=Mock(return_value=110),
            is_contract=Mock(side_effect=lambda address: address == contract_address),
            estimate_gas_limit=Mock(return_value=21000),
        )
        self.fake_client_selector.return_value = fake_client

        with self.subTest("Empty UnsignedTx"):
            self.assertEqual(
                UnsignedTx(fee_limit=21000, fee_price_per_unit=1),
                self.provider.fill_unsigned_tx(
                    UnsignedTx(),
                ),
            )
            fake_client.get_prices_per_unit_of_fee.assert_called_once()
            fake_client.get_address.assert_not_called()
            fake_client.is_contract.assert_not_called()
            fake_client.estimate_gas_limit.assert_not_called()
            fake_client.estimate_gas_and_collateral.assert_not_called()
            fake_client.get_epoch_number.assert_not_called()
            fake_client.get_prices_per_unit_of_fee.reset_mock()

        with self.subTest("Transfer CFX to external address with preset gas price"):
            self.assertEqual(
                UnsignedTx(
                    inputs=[TransactionInput(address=external_address_a, value=21)],
                    outputs=[TransactionOutput(address=external_address_b, value=21)],
                    nonce=10,
                    fee_price_per_unit=1,
                    fee_limit=21000,
                    payload={"storage_limit": 0, "epoch_height": 110},
                ),
                self.provider.fill_unsigned_tx(
                    UnsignedTx(
                        inputs=[TransactionInput(address=external_address_a, value=21)],
                        outputs=[TransactionOutput(address=external_address_b, value=21)],
                        fee_price_per_unit=1,
                    )
                ),
            )
            fake_client.get_prices_per_unit_of_fee.assert_not_called()
            fake_client.get_address.assert_called_once_with(external_address_a)
            fake_client.is_contract.assert_called_once_with(external_address_b)
            fake_client.estimate_gas_limit.assert_called_once_with(external_address_a, external_address_b, 21, None)
            fake_client.estimate_gas_and_collateral.assert_called_once_with(
                external_address_a, external_address_b, 21, None
            )
            fake_client.get_epoch_number.assert_called_once()

            fake_client.get_address.reset_mock()
            fake_client.is_contract.reset_mock()
            fake_client.estimate_gas_limit.reset_mock()
            fake_client.estimate_gas_and_collateral.reset_mock()
            fake_client.get_epoch_number.reset_mock()

        with self.subTest("Transfer CFX to external address with preset data"):
            fake_client.estimate_gas_limit.return_value = 21096
            self.assertEqual(
                UnsignedTx(
                    inputs=[TransactionInput(address=external_address_a, value=21)],
                    outputs=[TransactionOutput(address=external_address_b, value=21)],
                    nonce=10,
                    fee_price_per_unit=1,
                    fee_limit=21096,
                    payload={"data": b"OneKey", "storage_limit": 0, "epoch_height": 110},
                ),
                self.provider.fill_unsigned_tx(
                    UnsignedTx(
                        inputs=[TransactionInput(address=external_address_a, value=21)],
                        outputs=[TransactionOutput(address=external_address_b, value=21)],
                        fee_price_per_unit=1,
                        payload={"data": b"OneKey"},
                    )
                ),
            )
            fake_client.get_prices_per_unit_of_fee.assert_not_called()
            fake_client.get_address.assert_called_once_with(external_address_a)
            fake_client.is_contract.assert_called_once_with(external_address_b)
            fake_client.estimate_gas_limit.assert_called_once_with(
                external_address_a, external_address_b, 21, b"OneKey"
            )
            fake_client.estimate_gas_and_collateral.assert_called_once_with(
                external_address_a, external_address_b, 21, b"OneKey"
            )
            fake_client.get_epoch_number.assert_called_once()

            fake_client.get_address.reset_mock()
            fake_client.is_contract.reset_mock()
            fake_client.estimate_gas_limit.reset_mock()
            fake_client.estimate_gas_and_collateral.reset_mock()
            fake_client.get_epoch_number.reset_mock()

        with self.subTest("Transfer CFX to contract address with preset nonce"):
            fake_client.estimate_gas_limit.return_value = 60000
            self.assertEqual(
                UnsignedTx(
                    inputs=[TransactionInput(address=external_address_a, value=21)],
                    outputs=[TransactionOutput(address=contract_address, value=21)],
                    nonce=101,
                    fee_price_per_unit=1,
                    fee_limit=int(60000 * 1.2),
                    payload={"storage_limit": 0, "epoch_height": 110},
                ),
                self.provider.fill_unsigned_tx(
                    UnsignedTx(
                        inputs=[TransactionInput(address=external_address_a, value=21)],
                        outputs=[TransactionOutput(address=contract_address, value=21)],
                        nonce=101,
                    )
                ),
            )
            fake_client.get_prices_per_unit_of_fee.assert_called_once()
            fake_client.get_address.assert_not_called()
            fake_client.is_contract.assert_called_once_with(contract_address)
            fake_client.estimate_gas_limit.assert_called_once_with(external_address_a, contract_address, 21, None)
            fake_client.estimate_gas_and_collateral.assert_called_once_with(
                external_address_a, contract_address, 21, None
            )

            fake_client.get_epoch_number.assert_called_once()

            fake_client.get_prices_per_unit_of_fee.reset_mock()
            fake_client.is_contract.reset_mock()
            fake_client.estimate_gas_limit.reset_mock()
            fake_client.estimate_gas_and_collateral.reset_mock()
            fake_client.get_epoch_number.reset_mock()

        with self.subTest("Transfer CRC20 with preset gas price and lower gas limit"):
            fake_client.estimate_gas_limit.return_value = 60000
            erc20_transfer_data = "0xa9059cbb00000000000000000000000012b1e160ae592499b31a29b5cd98667780b066d60000000000000000000000000000000000000000000000000000000000000015"
            self.assertEqual(
                UnsignedTx(
                    inputs=[
                        TransactionInput(
                            address=external_address_a,
                            value=21,
                            token_address=contract_address,
                        )
                    ],
                    outputs=[
                        TransactionOutput(
                            address=external_address_b,
                            value=21,
                            token_address=contract_address,
                        )
                    ],
                    nonce=10,
                    fee_price_per_unit=1,
                    fee_limit=40000,  # Use the provided value
                    payload={"data": erc20_transfer_data, "storage_limit": 0, "epoch_height": 110},
                ),
                self.provider.fill_unsigned_tx(
                    UnsignedTx(
                        inputs=[
                            TransactionInput(
                                address=external_address_a,
                                value=21,
                                token_address=contract_address,
                            )
                        ],
                        outputs=[
                            TransactionOutput(
                                address=external_address_b,
                                value=21,
                                token_address=contract_address,
                            )
                        ],
                        fee_price_per_unit=1,
                        fee_limit=40000,
                    )
                ),
            )
            fake_client.get_prices_per_unit_of_fee.assert_not_called()
            fake_client.get_address.assert_called_once_with(external_address_a)
            fake_client.is_contract.assert_not_called()
            fake_client.estimate_gas_limit.assert_not_called()
            fake_client.estimate_gas_and_collateral.assert_called_once()
            fake_client.get_epoch_number.assert_called_once()

            fake_client.get_address.reset_mock()
            fake_client.estimate_gas_limit.reset_mock()
            fake_client.estimate_gas_and_collateral.reset_mock()
            fake_client.get_epoch_number.reset_mock()

    def test_sign_transaction(self):
        self.fake_chain_info.chain_id = 1

        with self.subTest("Sign CFX Transfer Tx"):
            fake_signer = Mock(
                sign=Mock(
                    return_value=(
                        bytes.fromhex(
                            "56ec975b033cfa511ab90acafa5cc1e37f0c90cb2a50c6463065e86f32571dae33865c56270d21ffb56dc87e5668a468f157f9d7a260f39947c96987591f6aef"
                        ),
                        0,
                    )
                )
            )
            signers = {"cfxtest:aakrn3d7hezu0tcafpe92bbbsy1fw2u5za1xa2uvgp": fake_signer}
            self.assertEqual(
                SignedTx(
                    txid="0x04b9f5bd832eb00b057604c768d3dd5a4dab11b0bb5265e00ca4a305e6f7c293",
                    raw_tx="0xf86ae60f018252089412b1e160ae592499b31a29b5cd98667780b066d6830f4240808401ef33b6018080a056ec975b033cfa511ab90acafa5cc1e37f0c90cb2a50c6463065e86f32571daea033865c56270d21ffb56dc87e5668a468f157f9d7a260f39947c96987591f6aef",
                ),
                self.provider.sign_transaction(
                    UnsignedTx(
                        inputs=[
                            TransactionInput(
                                address="cfxtest:aakrn3d7hezu0tcafpe92bbbsy1fw2u5za1xa2uvgp", value=1000000
                            )
                        ],
                        outputs=[
                            TransactionOutput(
                                address="cfxtest:aaknd2naz3pwkgrxdjy5nxp2p352bpdg42nr3cr2pf", value=1000000
                            )
                        ],
                        nonce=15,
                        fee_limit=21000,
                        fee_price_per_unit=1,
                        payload={
                            "storage_limit": 0,
                            "epoch_height": 32453558,
                        },
                    ),
                    signers,
                ),
            )

        with self.subTest("Sign CRC20 Transfer Tx"):
            fake_signer = Mock(
                sign=Mock(
                    return_value=(
                        bytes.fromhex(
                            "e98ae277c67e3a1cd170b2194077a2341b170ab86d3e7dc998ea5a2a446277a45834df1afd82a7558050dbbf58a671e73ec3ece64f35dbeb8f3cca6dc139b608"
                        ),
                        1,
                    )
                )
            )

            signers = {"cfxtest:aakrn3d7hezu0tcafpe92bbbsy1fw2u5za1xa2uvgp": fake_signer}
            self.assertEqual(
                SignedTx(
                    txid="0x0a12175e70fd78db5dd647b45d863680e33a1bc1f25c2902e68d25dfc9c7f38f",
                    raw_tx="0xf8aef869100182d75e9488c27bd05a7a58bafed6797efa0cce4e1d55302f8081808401ef379201b844a9059cbb00000000000000000000000012b1e160ae592499b31a29b5cd98667780b066d600000000000000000000000000000000000000000000000000000000000f424001a0e98ae277c67e3a1cd170b2194077a2341b170ab86d3e7dc998ea5a2a446277a4a05834df1afd82a7558050dbbf58a671e73ec3ece64f35dbeb8f3cca6dc139b608",
                ),
                self.provider.sign_transaction(
                    UnsignedTx(
                        inputs=[
                            TransactionInput(
                                address="cfxtest:aakrn3d7hezu0tcafpe92bbbsy1fw2u5za1xa2uvgp",
                                value=1000000,
                                token_address="cfxtest:acepe88unk7fvs18436178up33hb4zkuf62a9dk1gv",
                            )
                        ],
                        outputs=[
                            TransactionOutput(
                                address="cfxtest:aaknd2naz3pwkgrxdjy5nxp2p352bpdg42nr3cr2pf",
                                value=1000000,
                                token_address="cfxtest:acepe88unk7fvs18436178up33hb4zkuf62a9dk1gv",
                            )
                        ],
                        nonce=16,
                        fee_limit=55134,
                        fee_price_per_unit=1,
                        payload={
                            "data": "0xa9059cbb00000000000000000000000012b1e160ae592499b31a29b5cd98667780b066d600000000000000000000000000000000000000000000000000000000000f4240",
                            "storage_limit": 128,
                            "epoch_height": 32454546,
                        },
                    ),
                    signers,
                ),
            )
