import datetime
import decimal
from unittest import TestCase
from unittest.mock import Mock, call, patch

from electrum_gui.common.basic.orm import test_utils
from electrum_gui.common.coin import data as coin_data
from electrum_gui.common.provider import data as provider_data
from electrum_gui.common.transaction import daos, data, manager, models


@test_utils.cls_test_database(models.TxAction)
class TestTransactionManager(TestCase):
    @patch("electrum_gui.common.transaction.manager.provider_manager")
    def test_update_pending_actions(self, fake_provider_manager):
        # prepare data
        daos.bulk_create(
            [
                daos.new_action(
                    txid="txid_a",
                    status=data.TxActionStatus.PENDING,
                    chain_code="eth",
                    coin_code="eth",
                    value=decimal.Decimal(0),
                    decimals=decimal.Decimal(18),
                    symbol="ETH",
                    from_address="address_a",
                    to_address="contract_a",
                    fee_limit=decimal.Decimal(1000),
                    fee_price_per_unit=decimal.Decimal(20),
                    nonce=11,
                    raw_tx="",
                ),
                daos.new_action(
                    txid="txid_a",
                    status=data.TxActionStatus.PENDING,
                    chain_code="eth",
                    coin_code="eth_usdt",
                    value=decimal.Decimal(10),
                    decimals=decimal.Decimal(6),
                    symbol="USDT",
                    from_address="address_a",
                    to_address="address_b",
                    fee_limit=decimal.Decimal(1000),
                    fee_price_per_unit=decimal.Decimal(20),
                    index=1,
                    raw_tx="",
                ),
                daos.new_action(
                    txid="txid_b",
                    status=data.TxActionStatus.PENDING,
                    chain_code="bsc",
                    coin_code="bsc",
                    value=decimal.Decimal(3),
                    decimals=decimal.Decimal(18),
                    symbol="BSC",
                    from_address="address_b",
                    to_address="address_c",
                    fee_limit=decimal.Decimal(1000),
                    fee_price_per_unit=decimal.Decimal(20),
                    nonce=3,
                    raw_tx="",
                ),
                daos.new_action(
                    txid="txid_c",
                    status=data.TxActionStatus.PENDING,
                    chain_code="heco",
                    coin_code="heco",
                    value=decimal.Decimal(4),
                    decimals=decimal.Decimal(18),
                    symbol="HECO",
                    from_address="address_c",
                    to_address="address_b",
                    fee_limit=decimal.Decimal(1000),
                    fee_price_per_unit=decimal.Decimal(20),
                    nonce=3,
                    raw_tx="",
                ),
            ]
        )

        # Adjust the created time of the transaction to 3 days ago
        tx_bsc: models.TxAction = models.TxAction.get_or_none(txid="txid_b")
        models.TxAction.update(created_time=datetime.datetime.now() - datetime.timedelta(days=3)).where(
            models.TxAction.id == tx_bsc.id
        ).execute()

        fake_provider_manager.get_transaction_by_txid.side_effect = lambda i, j: {
            "eth": provider_data.Transaction(
                txid="txid_a",
                status=provider_data.TransactionStatus.CONFIRM_SUCCESS,
                fee=provider_data.TransactionFee(limit=1000, used=900, price_per_unit=20),
                block_header=provider_data.BlockHeader(
                    block_hash="block_a", block_number=1001, block_time=1600000000, confirmations=3
                ),
            ),
            "bsc": provider_data.Transaction(
                txid="txid_b",
                status=provider_data.TransactionStatus.PENDING,
            ),
            "heco": provider_data.Transaction(
                txid="txid_c",
                status=provider_data.TransactionStatus.CONFIRM_REVERTED,
                fee=provider_data.TransactionFee(limit=1000, used=1000, price_per_unit=20),
                block_header=provider_data.BlockHeader(block_hash="block_c", block_number=1010, block_time=1600000001),
            ),
        }.get(i)

        manager.update_pending_actions()

        txns = list(models.TxAction.select().order_by(models.TxAction.id.asc()))
        self.assertEqual(
            [
                {
                    "id": 1,
                    "txid": "txid_a",
                    "chain_code": "eth",
                    "coin_code": "eth",
                    "status": data.TxActionStatus.CONFIRM_SUCCESS,
                    "fee_used": decimal.Decimal(900),
                    "block_number": 1001,
                    "block_hash": "block_a",
                    "block_time": 1600000000,
                },
                {
                    "id": 2,
                    "txid": "txid_a",
                    "chain_code": "eth",
                    "coin_code": "eth_usdt",
                    "status": data.TxActionStatus.CONFIRM_SUCCESS,
                    "fee_used": decimal.Decimal(900),
                    "block_number": 1001,
                    "block_hash": "block_a",
                    "block_time": 1600000000,
                },
                {
                    "id": 3,
                    "txid": "txid_b",
                    "chain_code": "bsc",
                    "coin_code": "bsc",
                    "status": data.TxActionStatus.UNKNOWN,
                    "fee_used": decimal.Decimal(0),
                    "block_number": None,
                    "block_hash": None,
                    "block_time": None,
                },
                {
                    "id": 4,
                    "txid": "txid_c",
                    "chain_code": "heco",
                    "coin_code": "heco",
                    "status": data.TxActionStatus.CONFIRM_REVERTED,
                    "fee_used": decimal.Decimal(1000),
                    "block_number": 1010,
                    "block_hash": "block_c",
                    "block_time": 1600000001,
                },
            ],
            [
                {
                    "id": i.id,
                    "txid": i.txid,
                    "chain_code": i.chain_code,
                    "coin_code": i.coin_code,
                    "status": i.status,
                    "fee_used": i.fee_used,
                    "block_number": i.block_number,
                    "block_hash": i.block_hash,
                    "block_time": i.block_time,
                }
                for i in txns
            ],
        )
        fake_provider_manager.get_transaction_by_txid.assert_has_calls(
            [call('bsc', 'txid_b'), call('eth', 'txid_a'), call('heco', 'txid_c')]
        )

    @patch("electrum_gui.common.transaction.manager.coin_manager")
    def test_sync_address_actions_utxo_model(self, fake_coin_manager):
        fake_coin_manager.get_chain_info.return_value = Mock(chain_model=coin_data.ChainModel.UTXO)

        with self.assertRaisesRegex(
            Exception, "This chain model isn't supported yet. chain_code: btc, chain_model: <ChainModel.UTXO: 10>"
        ):
            manager.sync_address_actions("btc", "fake_btc_address")

    @patch("electrum_gui.common.transaction.manager.coin_manager")
    @patch("electrum_gui.common.transaction.manager.provider_manager")
    def test_sync_address_actions_account_model(self, fake_provider_manager, fake_coin_manager):
        fake_coin_manager.get_chain_info.return_value = Mock(chain_model=coin_data.ChainModel.ACCOUNT)
        fake_coin_manager.get_coin_info.return_value = Mock(code="eth", symbol="ETH", decimals=18)
        fake_coin_manager.query_coins_by_token_addresses.return_value = [
            Mock(code="eth_usdt", symbol="USDT", decimals=6, token_address="contract_a")
        ]

        fake_txs = [
            provider_data.Transaction(
                txid="txid_a",
                status=provider_data.TransactionStatus.CONFIRM_SUCCESS,
                inputs=[
                    provider_data.TransactionInput(address="address_a", value=0),
                    provider_data.TransactionInput(address="address_a", value=2100, token_address="contract_a"),
                ],
                outputs=[
                    provider_data.TransactionOutput(address="contract_a", value=0),
                    provider_data.TransactionOutput(address="address_b", value=2100, token_address="contract_a"),
                ],
                fee=provider_data.TransactionFee(limit=1000, used=900, price_per_unit=20),
                block_header=provider_data.BlockHeader(
                    block_hash="block_a", block_number=1001, block_time=1600000000, confirmations=3
                ),
                nonce=3,
            )
        ]
        fake_provider_manager.search_txs_by_address.return_value = fake_txs

        with self.subTest("First time"):
            manager.sync_address_actions("eth", "address_a")

            txns = list(models.TxAction.select().order_by(models.TxAction.id.asc()))
            self.assertEqual(2, len(txns))
            self.assertEqual(
                [
                    {
                        "id": 1,
                        "txid": "txid_a",
                        "status": models.TxActionStatus.CONFIRM_SUCCESS,
                        "chain_code": "eth",
                        "coin_code": "eth",
                        "value": decimal.Decimal(0),
                        "decimals": 18,
                        "from_address": "address_a",
                        "to_address": "contract_a",
                        "fee_limit": decimal.Decimal(1000),
                        "fee_used": decimal.Decimal(900),
                        "fee_price_per_unit": decimal.Decimal(20),
                        "block_number": 1001,
                        "block_hash": "block_a",
                        "block_time": 1600000000,
                        "index": 0,
                        "nonce": 3,
                    },
                    {
                        "id": 2,
                        "txid": "txid_a",
                        "status": models.TxActionStatus.CONFIRM_SUCCESS,
                        "chain_code": "eth",
                        "coin_code": "eth_usdt",
                        "value": decimal.Decimal(2100),
                        "decimals": 6,
                        "from_address": "address_a",
                        "to_address": "address_b",
                        "fee_limit": decimal.Decimal(1000),
                        "fee_used": decimal.Decimal(900),
                        "fee_price_per_unit": decimal.Decimal(20),
                        "block_number": 1001,
                        "block_hash": "block_a",
                        "block_time": 1600000000,
                        "index": 1,
                        "nonce": -1,
                    },
                ],
                [
                    {
                        "id": i.id,
                        "txid": i.txid,
                        "status": i.status,
                        "chain_code": i.chain_code,
                        "coin_code": i.coin_code,
                        "value": i.value,
                        "decimals": i.decimals,
                        "from_address": i.from_address,
                        "to_address": i.to_address,
                        "fee_limit": i.fee_limit,
                        "fee_used": i.fee_used,
                        "fee_price_per_unit": i.fee_price_per_unit,
                        "block_number": i.block_number,
                        "block_hash": i.block_hash,
                        "block_time": i.block_time,
                        "index": i.index,
                        "nonce": i.nonce,
                    }
                    for i in txns
                ],
            )
            fake_provider_manager.search_txs_by_address.assert_called_once_with("eth", "address_a", paginate=None)
            fake_provider_manager.search_txs_by_address.reset_mock()

        with self.subTest("Second time"):
            fake_txs = [
                provider_data.Transaction(
                    txid="txid_b",
                    status=provider_data.TransactionStatus.PENDING,
                    inputs=[provider_data.TransactionInput(address="address_a", value=11)],
                    outputs=[provider_data.TransactionOutput(address="contract_c", value=11)],
                    fee=provider_data.TransactionFee(limit=20000, used=1999, price_per_unit=20),
                    nonce=4,
                ),
                *fake_txs,
            ]
            fake_provider_manager.search_txs_by_address.return_value = fake_txs

            manager.sync_address_actions("eth", "address_a")
            txns = list(models.TxAction.select().order_by(models.TxAction.id.asc()))
            self.assertEqual(3, len(txns))
            last_one = txns[-1]
            self.assertEqual(
                {
                    "id": 3,
                    "txid": "txid_b",
                    "status": models.TxActionStatus.PENDING,
                    "chain_code": "eth",
                    "coin_code": "eth",
                    "value": decimal.Decimal(11),
                    "decimals": 18,
                    "from_address": "address_a",
                    "to_address": "contract_c",
                    "fee_limit": decimal.Decimal(20000),
                    "fee_used": decimal.Decimal(0),
                    "fee_price_per_unit": decimal.Decimal(20),
                    "block_number": None,
                    "block_hash": None,
                    "block_time": None,
                    "index": 0,
                    "nonce": 4,
                },
                {
                    "id": last_one.id,
                    "txid": last_one.txid,
                    "status": last_one.status,
                    "chain_code": last_one.chain_code,
                    "coin_code": last_one.coin_code,
                    "value": last_one.value,
                    "decimals": last_one.decimals,
                    "from_address": last_one.from_address,
                    "to_address": last_one.to_address,
                    "fee_limit": last_one.fee_limit,
                    "fee_used": last_one.fee_used,
                    "fee_price_per_unit": last_one.fee_price_per_unit,
                    "block_number": last_one.block_number,
                    "block_hash": last_one.block_hash,
                    "block_time": last_one.block_time,
                    "index": last_one.index,
                    "nonce": last_one.nonce,
                },
            )
            fake_provider_manager.search_txs_by_address.assert_called_once_with(
                "eth", "address_a", paginate=provider_data.TxPaginate(start_block_number=1001)
            )
