from unittest import TestCase

from electrum_gui.common.wallet.bip44 import BIP44Level, BIP44Path


class TestBIP44Path(TestCase):
    def test_to_bip44_path(self):
        self.assertEqual(BIP44Path(44, 60, 1).to_bip44_path(), "m/44'/60'/1'")
        self.assertEqual(BIP44Path(44, 60, 1, 2).to_bip44_path(), "m/44'/60'/1'/2")
        self.assertEqual(BIP44Path(44, 60, 1, 2, 3).to_bip44_path(), "m/44'/60'/1'/2/3")
        self.assertEqual(
            BIP44Path(44, 60, 1, 2, 3, last_hardened_level=BIP44Level.ADDRESS_INDEX).to_bip44_path(),
            "m/44'/60'/1'/2'/3'",
        )

    def test_from_bip44_path(self):
        self.assertEqual(BIP44Path.from_bip44_path("m/44'/60'/1'"), BIP44Path(44, 60, 1))
        self.assertEqual(BIP44Path.from_bip44_path("m/44'/60'/1'/2"), BIP44Path(44, 60, 1, 2))
        self.assertEqual(BIP44Path.from_bip44_path("m/44'/60'/1'/2/3"), BIP44Path(44, 60, 1, 2, 3))
        self.assertEqual(
            BIP44Path.from_bip44_path("m/44'/60'/1'/2'/3'"),
            BIP44Path(44, 60, 1, 2, 3, last_hardened_level=BIP44Level.ADDRESS_INDEX),
        )

    def test_index_of(self):
        path = BIP44Path(44, 60, 1, 2, 3)
        self.assertEqual(path.index_of(BIP44Level.ADDRESS_INDEX), 3)
        self.assertEqual(path.index_of(BIP44Level.CHANGE), 2)
        self.assertEqual(path.index_of(BIP44Level.ACCOUNT), 1)
        self.assertEqual(BIP44Path(44, 60, 1).index_of(BIP44Level.ADDRESS_INDEX), None)

    def test_next_sibling(self):
        path = BIP44Path(44, 60, 1, 2, 3)
        self.assertEqual(path.next_sibling(), BIP44Path(44, 60, 1, 2, 4))
        self.assertEqual(path.next_sibling().next_sibling(), BIP44Path(44, 60, 1, 2, 5))
        self.assertEqual(path.next_sibling(gap=10), BIP44Path(44, 60, 1, 2, 13))

    def test_to_target_level(self):
        path = BIP44Path(44, 60, 1, 2, 3)
        self.assertEqual(path.to_target_level(BIP44Level.ACCOUNT), BIP44Path(44, 60, 1))
        self.assertEqual(path.to_target_level(BIP44Level.CHANGE), BIP44Path(44, 60, 1, 2))
        self.assertEqual(
            path.to_target_level(BIP44Level.ACCOUNT).to_target_level(BIP44Level.ADDRESS_INDEX),
            BIP44Path(44, 60, 1, 0, 0),
        )
        self.assertEqual(
            path.to_target_level(BIP44Level.ACCOUNT).to_target_level(BIP44Level.ADDRESS_INDEX, value_filling_if_none=1),
            BIP44Path(44, 60, 1, 1, 1),
        )
        with self.assertRaisesRegex(ValueError, "The target level should higher than account level"):
            path.to_target_level(BIP44Level.COIN_TYPE)
