import asyncio
import hashlib
import json
import sys
import traceback
from typing import Union

import base64

from electrum.plugin import BasePlugin, hook
from electrum.crypto import aes_encrypt_with_iv, aes_decrypt_with_iv
from electrum.i18n import _
from electrum.util import log_exceptions, ignore_exceptions, make_aiohttp_session
from electrum.network import Network


class ErrorConnectingServer(Exception):
    def __init__(self, reason: Union[str, Exception] = None):
        self.reason = reason

    def __str__(self):
        header = _("Error connecting to {} server").format('Labels')
        reason = self.reason
        if isinstance(reason, BaseException):
            reason = repr(reason)
        return f"{header}: {reason}" if reason else header


class LabelsPlugin(BasePlugin):

    def __init__(self, parent, config, name):
        BasePlugin.__init__(self, parent, config, name)
        #self.target_host = 'labels.electrum.org'
        self.target_host = '39.105.86.163:8080'
        #self.target_host = '127.0.0.1:8080'
        self.wallets = {}
        self.get_wallet_loop = asyncio.get_event_loop()

    def encode(self, wallet, msg):
        password, iv, wallet_id, xpubkeys = self.wallets[wallet]
        encrypted = aes_encrypt_with_iv(password, iv, msg.encode('utf8'))
        return base64.b64encode(encrypted).decode()

    def decode(self, wallet, message):
        password, iv, wallet_id, xpubkeys = self.wallets[wallet]
        decoded = base64.b64decode(message)
        decrypted = aes_decrypt_with_iv(password, iv, decoded)
        return decrypted.decode('utf8')

    def get_nonce(self, wallet):
        # nonce is the nonce to be used with the next change
        nonce = wallet.db.get('wallet_nonce')
        if nonce is None:
            nonce = 1
            self.set_nonce(wallet, nonce)
        return nonce

    def set_nonce(self, wallet, nonce):
        self.logger.info(f"set {wallet.basename()} nonce to {nonce}")
        wallet.db.put("wallet_nonce", nonce)

    @hook
    def set_label(self, wallet, item, label):
        if wallet not in self.wallets:
            return
        if not item:
            return
        nonce = self.get_nonce(wallet)
        wallet_id = self.wallets[wallet][2]
        bundle = {"walletId": wallet_id,
                  "walletNonce": nonce,
                  "externalId": self.encode(wallet, item),
                  "encryptedLabel": self.encode(wallet, label)}
        asyncio.run_coroutine_threadsafe(self.do_post_safe("/label", bundle), wallet.network.asyncio_loop)
        # Caller will write the wallet
        self.set_nonce(wallet, nonce + 1)

    @ignore_exceptions
    @log_exceptions
    async def do_post_safe(self, *args):
        await self.do_post(*args)

    async def do_get(self, url="/wallets"):
        url = 'http://' + self.target_host + url
        network = Network.get_instance()
        proxy = network.proxy if network else None
        async with make_aiohttp_session(proxy) as session:
            async with session.get(url) as result:
                return await result.json()

    async def do_post(self, url="/wallets", data=None):
        url = 'http://' + self.target_host + url
        network = Network.get_instance()
        proxy = network.proxy if network else None
        async with make_aiohttp_session(proxy) as session:
            async with session.post(url, json=data) as result:
                try:
                    return await result.json()
                except Exception as e:
                    raise Exception('Could not decode: ' + await result.text()) from e

    async def push_thread(self, wallet):
        wallet_data = self.wallets.get(wallet, None)
        if not wallet_data:
            raise Exception('Wallet {} not loaded'.format(wallet))
        wallet_id = wallet_data[2]
        for xpub in wallet_data[4]:
           # xpubId = self.encode(wallet, xpub)
            bundle = {"xpubs": "",
                      "xpubId": xpub,
                      "walletId": wallet_id,
                      "walletType": wallet_data[3]}
            bundle_list = []
            for value in wallet_data[4]:
                bundle_list.append(value)
                bundle["xpubs"] = json.dumps(bundle_list)

            await self.do_post("/wallet", bundle)

    async def pull_thread(self, xpub):
        try:
            response = await self.do_get("/wallets/%s" % xpub)
            print("--111112222 response=%s" %response)
        except Exception as e:
            raise ErrorConnectingServer(e) from e
        if response["Walltes"] is None:
            self.logger.info('no wallets info')
            return
        result = {}
        for wallet in response["Walltes"]:
            try:
                xpubId = wallet['xpubId']
                walletId = wallet['WalletId']
                xpubs = wallet['Xpubs']
                walletType = wallet['WalletType']
            except:
                continue
            wallet_list = [walletType, xpubs]
            try:
                #json.dumps(walletType)
                json.dumps(walletId)
                json.dumps(wallet_list)
            except:
                self.logger.info(f'error: no json {xpubs}')
                continue
            result[walletId] = wallet_list
        self.logger.info(f"received {len(response)} wallets")
        print("wallet info is %s---" %result)
        return result

    @ignore_exceptions
    @log_exceptions
    async def pull_safe_thread(self, wallet, force):
        try:
            await self.pull_thread(wallet, force)
        except ErrorConnectingServer as e:
            self.logger.info(repr(e))

    @ignore_exceptions
    @log_exceptions
    async def push_safe_thread(self, wallet):
        try:
            await self.push_thread(wallet)
        except ErrorConnectingServer as e:
            self.logger.info(repr(e))

    def pull(self, xpub):
        return asyncio.run_coroutine_threadsafe(self.pull_thread(xpub), self.get_wallet_loop).result()

    def push(self, wallet):
        if not wallet.network: raise Exception(_('You are offline.'))
        return asyncio.run_coroutine_threadsafe(self.push_thread(wallet), wallet.network.asyncio_loop).result()

    def start_wallet(self, wallet, wallet_type):
        if not wallet.network: return  # 'offline' mode
        mpk = wallet.get_fingerprint()
        if not mpk:
            return
        mpk = mpk.encode('ascii')
        password = hashlib.sha1(mpk).hexdigest()[:32].encode('ascii')
        iv = hashlib.sha256(password).digest()[:16]
        wallet_id = hashlib.sha256(mpk).hexdigest()
        xpubkeys = wallet.get_master_public_keys()
        self.wallets[wallet] = (password, iv, wallet_id, wallet_type, xpubkeys)
       # self.push(wallet)
        # If there is an auth token we can try to actually start syncing
        asyncio.run_coroutine_threadsafe(self.push_safe_thread(wallet), wallet.network.asyncio_loop)

    def stop_wallet(self, wallet):
        self.wallets.pop(wallet, None)
        self.get_wallet_loop.close()
