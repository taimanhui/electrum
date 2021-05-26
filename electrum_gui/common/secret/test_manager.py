from unittest import TestCase
from unittest.mock import call, patch

from electrum_gui.common.basic.orm import test_utils
from electrum_gui.common.secret import exceptions
from electrum_gui.common.secret import manager as secret_manager
from electrum_gui.common.secret import utils
from electrum_gui.common.secret.data import CurveEnum, PubKeyType, SecretKeyType
from electrum_gui.common.secret.models import PubKeyModel, SecretKeyModel


@test_utils.cls_test_database(PubKeyModel, SecretKeyModel)
class TestSecretManager(TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.message = b"Hello OneKey"
        cls.mnemonic = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"
        cls.passphrase = "OneKey"
        cls.master_seed = bytes.fromhex(
            "ac7728a67cf7fe4a237668db29f7d93243da5cecd3e7cb790dc393e31fdfaadf8ced5e17bb53be83823faae50eb4fc4a8d67486fa04851238accc29005734692"
        )

        cls.account_level_path = "m/44'/60'/0'"
        cls.account_level_xpub = "xpub6CEGaxz3GGxefcUFP4mYo6f8BmAUywBSfYbLd8Z851RntgRrp2vhYaiddPFJR9a6TV2Fsz2PY3PbGHNMgnNTKQVPssfcaieBHnT8Leh47VR"
        cls.account_level_xprv = "xprv9yEvBTT9RuQMT8PnH3EYRxiPdjKzaUTbJKfjpk9WWftp1t6iGVcSznQ9n7Y6aLTjGf2P1pD3QDfugmjgVRw1d8t1MmZeaqArnKCUTMfovLR"
        cls.account_level_signature = (
            "19f8ab7d24d019bdf1443719447516961d4918cb441ef083e3531ad74f364b5303c070d6a0175751afb93fb32f21f666e652f89262139b5f474b34d9305d8ece",
            0,
        )

        cls.address_level_path = f"{cls.account_level_path}/0/0"
        cls.address_level_prvkey = "77f22e0d920c7b59df81a629dc75c27513b5360a45d55f3253454f5d3cb23bab"
        cls.address_level_pubkey = "02deb60902c06bfed8d78e33337be995d0b3efc28fbc61b6f88cb5cfb27dc4efd1"
        cls.address_level_signature = (
            "e1b1ed07c97cc6204cb9b5a5f446d4757bf3abc6bf48f886c7a96c303552575b41cc543b07ca36c6987121a9628ddd1d76ab603e7821b5f5ca83f063aba6bbf7",
            1,
        )

    def test_import_pubkey(self):
        pubkey_model = secret_manager.import_pubkey(CurveEnum.SECP256K1, bytes.fromhex(self.address_level_pubkey))

        models = list(PubKeyModel.select())
        self.assertEqual(len(models), 1)

        first_model: PubKeyModel = models[0]
        self.assertEqual(first_model.id, pubkey_model.id)
        self.assertEqual(first_model.curve, CurveEnum.SECP256K1)
        self.assertEqual(first_model.pubkey_type, PubKeyType.PUBKEY)
        self.assertEqual(first_model.pubkey, self.address_level_pubkey)
        self.assertIsNone(first_model.path)
        self.assertIsNone(first_model.parent_pubkey_id)
        self.assertIsNone(first_model.secret_key_id)

    def test_import_xpub(self):
        xpub_model = secret_manager.import_xpub(CurveEnum.SECP256K1, self.account_level_xpub, self.account_level_path)

        models = list(PubKeyModel.select())
        self.assertEqual(len(models), 1)

        first_model: PubKeyModel = models[0]
        self.assertEqual(first_model.id, xpub_model.id)
        self.assertEqual(first_model.curve, CurveEnum.SECP256K1)
        self.assertEqual(first_model.pubkey_type, PubKeyType.XPUB)
        self.assertEqual(first_model.pubkey, self.account_level_xpub)
        self.assertEqual(first_model.path, self.account_level_path)
        self.assertIsNone(first_model.parent_pubkey_id)
        self.assertIsNone(first_model.secret_key_id)

    def test_import_xpub_and_sub_pubkey(self):
        xpub_model = secret_manager.import_xpub(CurveEnum.SECP256K1, self.account_level_xpub, self.account_level_path)
        pubkey_model = secret_manager.import_pubkey(
            CurveEnum.SECP256K1,
            bytes.fromhex(self.address_level_pubkey),
            self.address_level_path,
            parent_pubkey_id=xpub_model.id,
        )

        models = list(PubKeyModel.select())
        self.assertEqual(len(models), 2)
        self.assertEqual(models[0], xpub_model)
        self.assertEqual(models[1], pubkey_model)

    @patch("electrum_gui.common.secret.manager.encrypt")
    def test_import_prvkey(self, fake_encrypt):
        fake_encrypt.encrypt_data.side_effect = lambda p, d: f"Encrypted<{p},{d}>"

        pubkey_model, secret_key_model = secret_manager.import_prvkey(
            "hello",
            CurveEnum.SECP256K1,
            bytes.fromhex(self.address_level_prvkey),
        )

        fake_encrypt.encrypt_data.assert_called_once_with("hello", self.address_level_prvkey)
        fake_encrypt.decrypt_data.assert_not_called()

        secret_key_models = list(SecretKeyModel.select())
        self.assertEqual(len(secret_key_models), 1)

        first_secret_key_model: SecretKeyModel = secret_key_models[0]
        self.assertEqual(first_secret_key_model.id, secret_key_model.id)
        self.assertEqual(first_secret_key_model.secret_key_type, SecretKeyType.PRVKEY)
        self.assertEqual(first_secret_key_model.encrypted_secret_key, f"Encrypted<hello,{self.address_level_prvkey}>")

        pubkey_models = list(PubKeyModel.select())
        self.assertEqual(len(pubkey_models), 1)

        first_pubkey_model: PubKeyModel = pubkey_models[0]
        self.assertEqual(first_pubkey_model.id, pubkey_model.id)
        self.assertEqual(first_pubkey_model.secret_key_id, secret_key_model.id)
        self.assertEqual(first_pubkey_model.pubkey_type, PubKeyType.PUBKEY)
        self.assertEqual(first_pubkey_model.pubkey, self.address_level_pubkey)
        self.assertEqual(first_pubkey_model.curve, CurveEnum.SECP256K1)
        self.assertIsNone(first_pubkey_model.path)
        self.assertIsNone(first_pubkey_model.parent_pubkey_id)

    @patch("electrum_gui.common.secret.manager.encrypt")
    def test_import_xprv(self, fake_encrypt):
        fake_encrypt.encrypt_data.side_effect = lambda p, d: f"Encrypted<{p},{d}>"

        pubkey_model, secret_key_model = secret_manager.import_xprv(
            "hello",
            CurveEnum.SECP256K1,
            self.account_level_xprv,
            path=self.account_level_path,
        )

        fake_encrypt.encrypt_data.assert_called_once_with("hello", self.account_level_xprv)
        fake_encrypt.decrypt_data.assert_not_called()

        secret_key_models = list(SecretKeyModel.select())
        self.assertEqual(len(secret_key_models), 1)

        first_secret_key_model: SecretKeyModel = secret_key_models[0]
        self.assertEqual(first_secret_key_model.id, secret_key_model.id)
        self.assertEqual(first_secret_key_model.secret_key_type, SecretKeyType.XPRV)
        self.assertEqual(first_secret_key_model.encrypted_secret_key, f"Encrypted<hello,{self.account_level_xprv}>")

        pubkey_models = list(PubKeyModel.select())
        self.assertEqual(len(pubkey_models), 1)

        first_pubkey_model: PubKeyModel = pubkey_models[0]
        self.assertEqual(first_pubkey_model.id, pubkey_model.id)
        self.assertEqual(first_pubkey_model.secret_key_id, secret_key_model.id)
        self.assertEqual(first_pubkey_model.pubkey_type, PubKeyType.XPUB)
        self.assertEqual(first_pubkey_model.pubkey, self.account_level_xpub)
        self.assertEqual(first_pubkey_model.curve, CurveEnum.SECP256K1)
        self.assertEqual(first_pubkey_model.path, self.account_level_path)
        self.assertIsNone(first_pubkey_model.parent_pubkey_id)

    @patch("electrum_gui.common.secret.manager.encrypt")
    def test_import_master_seed(self, fake_encrypt):
        fake_encrypt.encrypt_data.side_effect = lambda p, d: f"Encrypted<{p},{d}>"

        secret_key_model = secret_manager.import_mnemonic("hello", self.mnemonic, self.passphrase)

        fake_encrypt.encrypt_data.assert_has_calls(
            [
                call("hello", self.master_seed.hex()),
                call("hello", f"{self.mnemonic}|{self.passphrase}"),
            ]
        )
        fake_encrypt.decrypt_data.assert_not_called()

        secret_key_models = list(SecretKeyModel.select())
        self.assertEqual(len(secret_key_models), 1)

        first_secret_key_model: SecretKeyModel = secret_key_models[0]
        self.assertEqual(first_secret_key_model.id, secret_key_model.id)
        self.assertEqual(first_secret_key_model.secret_key_type, SecretKeyType.SEED)
        self.assertEqual(first_secret_key_model.encrypted_secret_key, f"Encrypted<hello,{self.master_seed.hex()}>")
        self.assertEqual(
            first_secret_key_model.encrypted_message,
            f"Encrypted<hello,{self.mnemonic}|{self.passphrase}>",
        )

    @patch("electrum_gui.common.secret.manager.encrypt")
    def test_export_mnemonic(self, fake_encrypt):
        fake_encrypt.encrypt_data.side_effect = lambda p, d: d
        fake_encrypt.decrypt_data.side_effect = lambda p, d: d

        secret_key_model = secret_manager.import_mnemonic("hello", self.mnemonic, self.passphrase)

        fake_encrypt.encrypt_data.assert_has_calls(
            [
                call("hello", self.master_seed.hex()),
                call("hello", f"{self.mnemonic}|{self.passphrase}"),
            ]
        )
        fake_encrypt.decrypt_data.assert_not_called()
        fake_encrypt.encrypt_data.reset_mock()

        mnemonic, passphrase = secret_manager.export_mnemonic("hello", secret_key_model.id)
        fake_encrypt.encrypt_data.assert_not_called()
        fake_encrypt.decrypt_data.assert_has_calls(
            [
                call("hello", f"{self.mnemonic}|{self.passphrase}"),
                call("hello", self.master_seed.hex()),
            ]
        )
        self.assertEqual(mnemonic, self.mnemonic)
        self.assertEqual(passphrase, self.passphrase)

    @patch("electrum_gui.common.secret.manager.encrypt")
    def test_derive_by_secret_key(self, fake_encrypt):
        fake_encrypt.encrypt_data.side_effect = lambda p, d: d
        fake_encrypt.decrypt_data.side_effect = lambda p, d: d

        secret_key_model = secret_manager.import_mnemonic("hello", self.mnemonic, self.passphrase)

        fake_encrypt.encrypt_data.assert_has_calls(
            [
                call("hello", self.master_seed.hex()),
                call("hello", f"{self.mnemonic}|{self.passphrase}"),
            ]
        )
        fake_encrypt.decrypt_data.assert_not_called()
        fake_encrypt.encrypt_data.reset_mock()

        pubkey_model = secret_manager.derive_by_secret_key(
            "hello", CurveEnum.SECP256K1, secret_key_model.id, self.account_level_path
        )  # Only create new object, without saving to the database

        fake_encrypt.encrypt_data.assert_not_called()
        fake_encrypt.decrypt_data.assert_called_once_with("hello", secret_key_model.encrypted_secret_key)

        self.assertEqual(len(PubKeyModel.select()), 0)
        self.assertIsNone(pubkey_model.id)
        self.assertEqual(pubkey_model.secret_key_id, secret_key_model.id)
        self.assertEqual(pubkey_model.pubkey_type, PubKeyType.XPUB)
        self.assertEqual(pubkey_model.pubkey, self.account_level_xpub)
        self.assertEqual(pubkey_model.curve, CurveEnum.SECP256K1)
        self.assertEqual(pubkey_model.path, self.account_level_path)
        self.assertIsNone(pubkey_model.parent_pubkey_id)

    @patch("electrum_gui.common.secret.manager.encrypt")
    def test_derive_by_xpub(self, fake_encrypt):
        fake_encrypt.encrypt_data.side_effect = lambda p, d: d

        secret_key_model = secret_manager.import_mnemonic("hello", self.mnemonic, self.passphrase)

        fake_encrypt.encrypt_data.assert_has_calls(
            [
                call("hello", self.master_seed.hex()),
                call("hello", f"{self.mnemonic}|{self.passphrase}"),
            ]
        )
        fake_encrypt.decrypt_data.assert_not_called()
        fake_encrypt.encrypt_data.reset_mock()

        xpub_model = secret_manager.import_xpub(
            CurveEnum.SECP256K1, self.account_level_xpub, self.account_level_path, secret_key_id=secret_key_model.id
        )
        fake_encrypt.encrypt_data.assert_not_called()
        fake_encrypt.decrypt_data.assert_not_called()

        sub_path = utils.diff_bip32_paths(self.account_level_path, self.address_level_path)
        self.assertEqual(sub_path, "m/0/0")

        pubkey_model = secret_manager.derive_by_xpub(
            xpub_model.id, sub_path, target_pubkey_type=PubKeyType.PUBKEY
        )  # Only create new object, without saving to the database
        fake_encrypt.encrypt_data.assert_not_called()
        fake_encrypt.decrypt_data.assert_not_called()

        self.assertEqual(len(PubKeyModel.select()), 1)
        self.assertIsNone(pubkey_model.id)
        self.assertEqual(pubkey_model.secret_key_id, secret_key_model.id)
        self.assertEqual(pubkey_model.pubkey_type, PubKeyType.PUBKEY)
        self.assertEqual(pubkey_model.pubkey, self.address_level_pubkey)
        self.assertEqual(pubkey_model.curve, CurveEnum.SECP256K1)
        self.assertEqual(pubkey_model.path, self.address_level_path)
        self.assertEqual(pubkey_model.parent_pubkey_id, xpub_model.id)

    @patch("electrum_gui.common.secret.manager.encrypt")
    def test_get_verifier(self, fake_encrypt):
        xpub_model = secret_manager.import_xpub(CurveEnum.SECP256K1, self.account_level_xpub, self.account_level_path)
        pubkey_model = secret_manager.import_pubkey(
            CurveEnum.SECP256K1,
            bytes.fromhex(self.address_level_pubkey),
            self.address_level_path,
            parent_pubkey_id=xpub_model.id,
        )
        fake_encrypt.encrypt_data.assert_not_called()
        fake_encrypt.decrypt_data.assert_not_called()

        self.assertTrue(
            secret_manager.get_verifier(xpub_model.id).verify(
                self.message, bytes.fromhex(self.account_level_signature[0])
            )
        )
        self.assertTrue(
            secret_manager.get_verifier(pubkey_model.id).verify(
                self.message, bytes.fromhex(self.address_level_signature[0])
            )
        )
        fake_encrypt.encrypt_data.assert_not_called()
        fake_encrypt.decrypt_data.assert_not_called()

    @patch("electrum_gui.common.secret.manager.encrypt")
    def test_get_signer(self, fake_encrypt):
        fake_encrypt.encrypt_data.side_effect = lambda p, d: d
        fake_encrypt.decrypt_data.side_effect = lambda p, d: d

        # 1. import mnemonic
        secret_key_model = secret_manager.import_mnemonic("hello", self.mnemonic, self.passphrase)

        fake_encrypt.encrypt_data.assert_has_calls(
            [
                call("hello", self.master_seed.hex()),
                call("hello", f"{self.mnemonic}|{self.passphrase}"),
            ]
        )
        fake_encrypt.decrypt_data.assert_not_called()
        fake_encrypt.encrypt_data.reset_mock()

        # 2. derive account level xpub from master seed
        temp_account_level_xpub = secret_manager.derive_by_secret_key(
            "hello", CurveEnum.SECP256K1, secret_key_model.id, self.account_level_path
        )
        imported_account_level_xpub = secret_manager.import_xpub(
            temp_account_level_xpub.curve,
            temp_account_level_xpub.pubkey,
            path=temp_account_level_xpub.path,
            parent_pubkey_id=temp_account_level_xpub.parent_pubkey_id,
            secret_key_id=temp_account_level_xpub.secret_key_id,
        )

        fake_encrypt.encrypt_data.assert_not_called()
        fake_encrypt.decrypt_data.assert_called_once_with(
            "hello", secret_key_model.encrypted_secret_key
        )  # Only expose the seed when generating xpub
        fake_encrypt.decrypt_data.reset_mock()

        # 3. derive address level pubkey from account level xpub
        temp_address_level_pubkey = secret_manager.derive_by_xpub(
            imported_account_level_xpub.id,
            utils.diff_bip32_paths(self.account_level_path, self.address_level_path),
            PubKeyType.PUBKEY,
        )
        imported_address_level_pubkey = secret_manager.import_pubkey(
            temp_address_level_pubkey.curve,
            bytes.fromhex(temp_address_level_pubkey.pubkey),
            temp_address_level_pubkey.path,
            temp_address_level_pubkey.parent_pubkey_id,
            temp_address_level_pubkey.secret_key_id,
        )

        fake_encrypt.encrypt_data.assert_not_called()
        fake_encrypt.decrypt_data.assert_not_called()

        signer = secret_manager.get_signer("hello", imported_address_level_pubkey.id)
        fake_encrypt.encrypt_data.assert_not_called()
        fake_encrypt.decrypt_data.assert_called_once_with("hello", secret_key_model.encrypted_secret_key)

        self.assertEqual(
            signer.sign(self.message), (bytes.fromhex(self.address_level_signature[0]), self.address_level_signature[1])
        )

    def test_update_secret_key_password(self):
        # 1. import mnemonic
        secret_key_model = secret_manager.import_mnemonic("hello", self.mnemonic, self.passphrase)

        # 2. raise if password illegal
        with self.assertRaises(exceptions.InvalidPassword):
            secret_manager.update_secret_key_password(secret_key_model.id, "HELLO", "bye")

        # 3. change password successfully
        secret_manager.update_secret_key_password(secret_key_model.id, "hello", "bye")

        # 4. it can be called multiple times, as long as the new password is correct
        secret_manager.update_secret_key_password(secret_key_model.id, "hello", "bye")
        secret_manager.update_secret_key_password(secret_key_model.id, "hello2", "bye")
