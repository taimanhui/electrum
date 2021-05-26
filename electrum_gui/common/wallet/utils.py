from electrum_gui.common.wallet import exceptions


def decrypt_eth_keystore(keyfile_json: str, keystore_password: str) -> bytes:
    try:
        import eth_account

        return bytes(eth_account.account.Account.decrypt(keyfile_json, keystore_password))
    except Exception as e:
        raise exceptions.DecryptingKeystoreException(e)
