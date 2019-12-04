from __future__ import absolute_import, division, print_function

from code import InteractiveConsole
import json
import os
from os.path import exists, join
import pkgutil
import unittest

from electrum.plugin import Plugins
from electrum.transaction import Transaction, TxOutput
from electrum import commands, daemon, keystore, simple_config, storage, tests, util
from electrum.util import Fiat
from electrum import MutiBase
from electrum.i18n import _
from electrum.storage import WalletStorage
from electrum.wallet import (ImportedAddressWallet, ImportedPrivkeyWallet, Standard_Wallet,
                                 Wallet)
from electrum.bitcoin import is_address,  hash_160, COIN, TYPE_ADDRESS

#from android.preference import PreferenceManager
from electrum.commands import satoshis
from electrum.bip32 import BIP32Node, convert_bip32_path_to_list_of_uint32 as parse_path
from electrum.network import Network, TxBroadcastError, BestEffortRequestFailed
import trezorlib.btc
from electrum import ecc

from enum import Enum
class Status(Enum):
    net = 1
    broadcast = 2
    sign = 3
    update_wallet = 4
    update_status = 5
    update_history = 6
    update_interfaces = 7
    create_wallet_error = 8



class AndroidConsole(InteractiveConsole):
    """`interact` must be run on a background thread, because it blocks waiting for input.
    """
    def __init__(self, app, cmds):
        namespace = dict(c=cmds, context=app)
        namespace.update({name: CommandWrapper(cmds, name) for name in all_commands})
        namespace.update(help=Help())
        InteractiveConsole.__init__(self, locals=namespace)

    def interact(self):
        try:
            InteractiveConsole.interact(
                self, banner=(
                    _("WARNING!") + "\n" +
                    _("Do not enter code here that you don't understand. Executing the wrong "
                      "code could lead to your coins being irreversibly lost.") + "\n" +
                    "Type 'help' for available commands and variables."))
        except SystemExit:
            pass


class CommandWrapper:
    def __init__(self, cmds, name):
        self.cmds = cmds
        self.name = name

    def __call__(self, *args, **kwargs):
        return getattr(self.cmds, self.name)(*args, **kwargs)


class Help:
    def __repr__(self):
        return self.help()

    def __call__(self, *args):
        print(self.help(*args))

    def help(self, name_or_wrapper=None):
        if name_or_wrapper is None:
            return("Commands:\n" +
                   "\n".join(f"  {cmd}" for name, cmd in sorted(all_commands.items())) +
                   "\nType help(<command>) for more details.\n"
                   "The following variables are also available: "
                   "c.config, c.daemon, c.network, c.wallet, context")
        else:
            if isinstance(name_or_wrapper, CommandWrapper):
                cmd = all_commands[name_or_wrapper.name]
            else:
                cmd = all_commands[name_or_wrapper]
            return f"{cmd}\n{cmd.description}"


# Adds additional commands which aren't available over JSON RPC.
class AndroidCommands(commands.Commands):
    def __init__(self):
        config_options = {}
        # config_options['cmdname'] = 'daemon'
        # config_options['testnet'] = True
        # config_options['cwd'] = os.getcwd()
        config_options['auto_connect'] = True
        self.config = simple_config.SimpleConfig(config_options)

        fd, server = daemon.get_fd_or_server(self.config)
        if not fd:
            raise Exception("Daemon already running")  # Same wording as in daemon.py.

        # Initialize here rather than in start() so the DaemonModel has a chance to register
        # its callback before the daemon threads start.
        self.daemon = daemon.Daemon(self.config, fd, False)
        self.network = self.daemon.network
        #self.network.register_callback(self._on_callback, CALLBACKS)
        self.daemon_running = False
        self.wizard = None
        self.plugin = Plugins(self.config, 'cmdline')
        self.callbackIntent = None
        self.wallet = None
        self.fiat_unit = self.daemon.fx.ccy if self.daemon.fx.is_enabled() else ''
        self.decimal_point = self.config.get('decimal_point', util.DECIMAL_POINT_DEFAULT)
        self.num_zeros = int(self.config.get('num_zeros', 0))
        self.rbf = self.config.get("use_rbf", True)

        if self.network:
            interests = ['wallet_updated', 'network_updated', 'blockchain_updated',
                         'status', 'new_transaction', 'verified']
            self.network.register_callback(self.on_network_event, interests)
            self.network.register_callback(self.on_fee, ['fee'])
            self.network.register_callback(self.on_fee_histogram, ['fee_histogram'])
            self.network.register_callback(self.on_quotes, ['on_quotes'])
            self.network.register_callback(self.on_history, ['on_history'])

    # BEGIN commands from the argparse interface.

    def on_fee(self, event, *arg):
        self.fee_status = self.config.get_fee_status()

    def on_fee_histogram(self, *args):
        self.update_history()

    def on_quotes(self, d):
        self.update_status()
        self.update_history()

    def on_history(self, d):
        if self.wallet:
            self.wallet.clear_coin_price_cache()
        self.update_history()

    def update_status(self):
        if not self.wallet:
            return
        if self.network is None or not self.network.is_connected():
            status = _("Offline")
        elif self.network.is_connected():
            self.num_blocks = self.network.get_local_height()
            server_height = self.network.get_server_height()
            server_lag = self.num_blocks - server_height
            if not self.wallet.up_to_date or server_height == 0:
                num_sent, num_answered = self.wallet.get_history_sync_state_details()
                status = ("{} [size=18dp]({}/{})[/size]"
                          .format(_("Synchronizing..."), num_answered, num_sent))
            elif server_lag > 1:
                status = _("Server is lagging ({} blocks)").format(server_lag)
            else:
                status = ''
        else:
            status = _("Disconnected")
        if status:
            self.balance = status
            self.fiat_balance = status
        else:
            c, u, x = self.wallet.get_balance()
            text = self.format_amount(c+x+u)
            self.balance = str(text.strip()) + ' [size=22dp]%s[/size]'% self.base_unit
            self.fiat_balance = self.daemon.fx.format_amount(c+u+x) + ' [size=22dp]%s[/size]'% self.daemon.fx.ccy
        #self.callbackIntent.onCallback("update_status")

    def get_wallet_info(self):
        wallet_info = {}
        wallet_info['balance'] = self.balance
        wallet_info['fiat_balance'] = self.fiat_balance
        wallet_info['name'] = self.wallet.basename()
        return json.dumps(wallet_info)

    def update_interfaces(self):
        net_params = self.network.get_parameters()
        self.num_nodes = len(self.network.get_interfaces())
        self.num_chains = len(self.network.get_blockchains())
        chain = self.network.blockchain()
        self.blockchain_forkpoint = chain.get_max_forkpoint()
        self.blockchain_name = chain.get_name()
        interface = self.network.interface
        if interface:
            self.server_host = interface.host
        else:
            self.server_host = str(net_params.host) + ' (connecting...)'
        self.proxy_config = net_params.proxy or {}
        mode = self.proxy_config.get('mode')
        host = self.proxy_config.get('host')
        port = self.proxy_config.get('port')
        self.proxy_str = (host + ':' + port) if mode else _('None')
        #self.callbackIntent.onCallback("update_interfaces")

    def update_wallet(self):
        self.update_status()
        #self.callbackIntent.onCallback("update_wallet")

    def update_history(self):
        print("")
        #self.callbackIntent.onCallback("update_history")

    def on_network_event(self, event, *args):
        if event == 'network_updated':
            self.update_interfaces()
            self.update_status()
        elif event == 'wallet_updated':
            self.update_status()
            self.update_wallet()
        elif event == 'blockchain_updated':
            # to update number of confirmations in history
            self.update_wallet()
        elif event == 'status':
            self.update_status()
        elif event == 'new_transaction':
            self.update_wallet()
        elif event == 'verified':
            self.update_wallet()

    def start(self):
        """Start the daemon"""
        self.daemon.start()
        self.daemon_running = True                      

    def status(self):
        """Get daemon status"""
        try:
            self._assert_daemon_running()
        except Exception as e:
            raise e
        return self.daemon.run_daemon({"subcommand": "status"})

    def stop(self):
        """Stop the daemon"""
        try:
            self._assert_daemon_running()
        except Exception as e:
            raise e
        self.daemon.stop()
        self.daemon.join()
        self.daemon_running = False


    def load_wallet(self, name, password=None):
        """Load a wallet"""
        print("console.load_wallet_by_name in....")
        try:
            self._assert_daemon_running()
        except Exception as e:
            raise e
        path = self._wallet_path(name)
        wallet = self.daemon.get_wallet(path)
        if not wallet:
            storage = WalletStorage(path)
            if not storage.file_exists():
                raise Exception("not find file %s" %path)
            if storage.is_encrypted():
                if not password:
                    raise util.InvalidPassword()
                storage.decrypt(password)

            wallet = Wallet(storage)
            wallet.start_network(self.network)
            self.daemon.add_wallet(wallet)

    def close_wallet(self, name=None):
        """Close a wallet"""
        try:
            self._assert_daemon_running()
        except Exception as e:
            raise e
        self.daemon.stop_wallet(self._wallet_path(name))

    ##set callback##############################
    def set_callback_fun(self, callbackIntent):
        print("self.callbackIntent =%s" %callbackIntent)
        self.callbackIntent = callbackIntent

    #craete multi wallet##############################
    def set_multi_wallet_info(self, name, m, n):
        try:
            self._assert_daemon_running()
        except Exception as e:
            raise e
        if self.wizard is not None:
            self.wizard = None
        self.wizard = MutiBase.MutiBase(self.config)
        path = self._wallet_path(name)
        print("console:set_multi_wallet_info:path = %s---------" % path)
        self.wizard.set_multi_wallet_info(path, m, n)

    def add_xpub(self, xpub):
        try:
            self._assert_daemon_running()
            self._assert_wizard_isvalid()
            self.wizard.restore_from_xpub(xpub)
        except Exception as e:
            raise e

    def delete_xpub(self, xpub):
        try:
            self._assert_daemon_running()
            self._assert_wizard_isvalid()
            self.wizard.delete_xpub(xpub)
        except Exception as e:
            raise e

    def get_keystores_info(self):
        try:
            self._assert_daemon_running()
            self._assert_wizard_isvalid()
            ret = self.wizard.get_keystores_info()
        except Exception as e:
            raise e
        return ret

    def get_cosigner_num(self):
        try:
            self._assert_daemon_running()
            self._assert_wizard_isvalid()
        except Exception as e:
            raise e
        return self.wizard.get_cosigner_num()

    def create_multi_wallet(self, name):
        try:
            self._assert_daemon_running()
            self._assert_wizard_isvalid()
            path = self._wallet_path(name)
            print("console:create_multi_wallet:path = %s---------" % path)
            storage = self.wizard.create_storage(path=path, password = '')
        except Exception as e:
            raise e
        if storage:
            wallet = Wallet(storage)
            wallet.start_network(self.daemon.network)
            self.daemon.add_wallet(wallet)
            if self.wallet:
                self.close_wallet()
            self.wallet = wallet
            self.wallet_name = wallet.basename()
            print("console:create_multi_wallet:wallet_name = %s---------" % self.wallet_name)
            self.select_wallet(self.wallet_name)
        self.wizard = None

    ##create tx#########################
    def mktx(self, outputs, message, fee):
        try:
            self._assert_wallet_isvalid()
        except Exception as e:
            raise e
        print("console.mktx.outpus = %s======" %outputs)
        all_output_add = json.loads(outputs)
        outputs_addrs = []
        for address, amount in all_output_add:
            outputs_addrs.append(TxOutput(TYPE_ADDRESS, address, satoshis(amount)))
        #outputs_addrs = [(TxOutput(TYPE_ADDRESS, "tb1qwz3zcty8txqw077mckv5wycf2tj697ncnjwp9m", satoshis(0.01)))]
        print("console.mktx[%s] wallet_type = %s use_change=%s add = %s" %(self.wallet, self.wallet.wallet_type,self.wallet.use_change, self.wallet.get_addresses()))
        coins = self.wallet.get_spendable_coins(None, self.config)
        try:
            tx = self.wallet.make_unsigned_transaction(coins, outputs_addrs, self.config, fixed_fee=satoshis(fee))
        except Exception as e:
            raise e
        tx.set_rbf(self.rbf)
        print("console:mkun:tx====%s" %tx)
        self.wallet.set_label(tx.txid, message)
        tx_details = self.wallet.get_tx_info(tx)
        ret_data = {
            'amount':tx_details.amount,
            'fee':tx_details.fee,
            'tx':str(tx)
        }
        json_str = json.dumps(ret_data)
        return json_str

    def deserialize(self, raw_tx):
        try:
            tx = Transaction(raw_tx)
            tx.deserialize()
        except Exception as e:
            raise e

    def get_wallets_list_info(self):
        try:
            self._assert_daemon_running()
        except Exception as e:
            raise e
        wallets = self.list_wallets()
        wallet_info = []
        for i in wallets:
            path = self._wallet_path(i)
            wallet = self.daemon.get_wallet(path)
            if not wallet:
                storage = WalletStorage(path)
                if not storage.file_exists():
                    raise Exception("not find file %s" %path)

                try:
                    wallet = Wallet(storage)
                except Exception as e:
                    raise e
            c, u, x = wallet.get_balance()
            info = {
                "wallet_type" : wallet.wallet_type,
                "balance" : self.format_amount_and_units(c),
                "name" : i
            }
            wallet_info.append(info)
        print("wallet_info = %s ............" % wallet_info)
        return json.dumps(wallet_info)

    def format_amount(self, x, is_diff=False, whitespaces=False):
        return util.format_satoshis(x, self.num_zeros, self.decimal_point, is_diff=is_diff, whitespaces=whitespaces)

    def base_unit(self):
        return util.decimal_point_to_base_unit_name(self.decimal_point)

    def format_amount_and_units(self, amount):
        try:
            self._assert_daemon_running()
        except Exception as e:
            raise e
        text = self.format_amount(amount) + ' '+ self.base_unit()
        x = self.daemon.fx.format_amount_and_units(amount) if self.daemon.fx else None
        if text and x:
            text += ' (%s)'%x
        return text

    ##qr api
    def get_raw_tx_from_qr_data(self, data):
        from electrum.util import bh2u
        return bh2u(base_decode(data, None, base=43))

    def get_qr_data_from_raw_tx(self, raw_tx):
        from electrum.bitcoin import base_encode, bfh
        text = bfh(raw_tx)
        return base_encode(text, base=43)

    ## get tx info from raw_tx
    def get_tx_info_from_raw(self, raw_tx):
        from electrum.transaction import Transaction
        try:
            tx = Transaction(raw_tx)
            print("console:get_tx_info_from_raw:tx===%s" %tx)
            #tx.deserialize()
        except Exception as e:
            tx = None
            raise e
        return self.get_details_info(tx)

    def get_details_info(self, tx):
        try:
            self._assert_wallet_isvalid()
        except Exception as e:
            raise e
        tx_details = self.wallet.get_tx_info(tx)
        s, r = tx.signature_count()
        ret_data = {
            'txid':tx_details.txid,
            'can_broadcast':tx_details.can_broadcast,
            'amount': tx_details.amount,
            'fee': tx_details.fee,
            'description':tx_details.label,
            'tx_status':tx_details.status,#TODO:需要对应界面的几个状态
            'sign_status':[s,r],
            'output_addr':tx.get_outputs_for_UI(),
            'input_addr':[txin.get("address") for txin in tx.inputs()],
            'cosigner':[x.xpub for x in self.wallet.get_keystores()],
        }
        json_data = json.dumps(ret_data)
        return json_data

    ##history
    def get_history_tx(self):
        try:
            self._assert_wallet_isvalid()
        except Exception as e:
            raise e
        history = reversed(self.wallet.get_history())
        all_data = [self.get_card(*item) for item in history]
        print("console:get_history_tx:data = %s==========" % all_data)

    def get_tx_info(self, tx_hash):
        try:
            self._assert_wallet_isvalid()
        except Exception as e:
            raise e
        tx = self.wallet.db.get_transaction(tx_hash)
        if not tx:
            raise Exception('get transaction info failed')
        data = self.get_details_info(tx)
        print("console:get_tx_info:tx_details_data = %s..........." % data)

    def get_card(self, tx_hash, tx_mined_status, value, balance):
        try:
            self._assert_wallet_isvalid()
            self._assert_daemon_running()
        except Exception as e:
            raise e
        status, status_str = self.wallet.get_tx_status(tx_hash, tx_mined_status)
        label = self.wallet.get_label(tx_hash) if tx_hash else _('Pruned transaction outputs')
        ri = {}
        ri['tx_hash'] = tx_hash
        ri['date'] = status_str
        ri['message'] = label
        ri['confirmations'] = tx_mined_status.conf
        if value is not None:
            ri['is_mine'] = value < 0
            if value < 0: value = - value
            ri['amount'] = self.format_amount_and_units(value)
            if self.fiat_unit:
                fx = self.daemon.fx
                fiat_value = value / Decimal(bitcoin.COIN) * self.wallet.price_at_timestamp(tx_hash,
                                                                                                fx.timestamp_rate)
                fiat_value = Fiat(fiat_value, fx.ccy)
                ri['quote_text'] = fiat_value.to_ui_string()
        return ri

    def get_wallet_address_show_UI(self):#TODO:需要按照electrum方式封装二维码数据?
        try:
            self._assert_wallet_isvalid()
            data = util.create_bip21_uri(self.wallet.get_addresses()[0], "", "")
        except Exception as e:
            raise e
        return data

    ##Analyze QR data
    def parse_qr(self, data):
        from electrum.bitcoin import base_decode, is_address
        data = data.strip()
        ret_data = {}
        npos = data.find("bitcoin:")
        if npos != -1:
            npos1 = data.find("?", 1)
            if npos1 == -1:
                npos1 = len(data)
            add = data[npos+len("bitcoin:"):npos1]
            if is_address(data[npos+len("bitcoin:"):npos1]):
                ret_data['status'] = 1
                try:
                    uri = util.parse_URI(data)
                except Exception as e:
                    raise e
                ret_data['data'] = uri['address']
                return json.dumps(ret_data)

        ret_data['status'] = 2
        # try to decode transaction
        from electrum.transaction import Transaction
        from electrum.util import bh2u
        try:
            text = bh2u(base_decode(data, None, base=43))
            tx = Transaction(text)
            #tx.deserialize()
        except Exception as e:
            tx = None
            raise e
        if tx:
            data = self.get_details_info(tx)
            ret_data['data'] = data
            return json.dumps(ret_data)

    def broadcast_tx(self):
        if self.network and self.network.is_connected():
            status = False
            try:
                self.network.run_from_another_thread(self.network.broadcast_transaction(tx))
            except TxBroadcastError as e:
                msg = e.get_message_for_gui()
            except BestEffortRequestFailed as e:
                msg = repr(e)
            else:
                status, msg = True, tx.txid()
                self.callbackIntent.onCallback(Status.broadcast, msg)
        else:
            raise Exception(('Cannot broadcast transaction') + ':\n' + ('Not connected'))

    ## setting
    def set_rbf(self, status_rbf):
        use_rbf = self.config.get('use_rbf', True)
        if use_rbf == status_rbf :
            return
        self.config.set_key('use_rbf', status_rbf, True)
        self.rbf = status_rbf

    def set_use_change(self, status_change):
        try:
            self._assert_wallet_isvalid()
        except Exception as e:
            raise e
        use_change = self.config.get('use_change')
        if use_change == status_change :
            return
        self.config.set_key('use_change', status_change, True)
        self.wallet.use_change = status_change

    def sign_tx(self, tx):
        try:
            self._assert_wallet_isvalid()
            tx = Transaction(tx)
            #tx.deserialize()
            self.wallet.sign_transaction(tx, None)
        except Exception as e:
            raise e
    ##connection with terzorlib#########################
    def get_xpub_from_hw(self):
        plugin = self.plugin.get_plugin("trezor")
        xpub = plugin.get_xpub('', '', 'standard', plugin.handler)

        #import usb1
        devices = self.get_connected_hw_devices(self.plugin)
        print("console:get_xpub_from_hw:devices=%s=====" % devices)
        if len(devices) == 0:
            print("Error: No connected hw device found. Cannot decrypt this wallet.")
            import sys
            sys.exit(1)
        elif len(devices) > 1:
            print("Warning: multiple hardware devices detected. "
                      "The first one will be used to decrypt the wallet.")
        # FIXME we use the "first" device, in case of multiple ones
        name, device_info = devices[0]
        plugin = self.plugin.get_plugin("trezor")
        print("console:get_xpub_from_hw:plugin=%s=====" % plugin)
        from electrum.storage import get_derivation_used_for_hw_device_encryption
        derivation = get_derivation_used_for_hw_device_encryption()
        print("console:get_xpub_from_hw:derivation=%s=====" % derivation)
        xpub = plugin.get_xpub(device_info.device.id_, derivation, 'standard', plugin.handler)
        print("console:get_xpub_from_hw:xpub=%s=====" % xpub)
        return xpub

    def get_connected_hw_devices(self, plugins):
        print("console.get_connected_hw_devices:plugins=%s------" % plugins)
        supported_plugins = plugins.get_hardware_support()
        # scan devices
        devices = []
        devmgr = plugins.device_manager
        for splugin in supported_plugins:
            name, plugin = splugin.name, splugin.plugin
            print("console:get_connected_hw_devices:name=%s plugin=%s" %(name, plugin))
            if not plugin:
                e = splugin.exception
                print("error during plugin init: error = %s======="%e)
                continue
            try:
                u = devmgr.unpaired_device_infos(None, plugin)
            except Exception as e:
                print("error getting device infos error=%s===========" % e)
                #_logger.error(f'error getting device infos for {name}: {repr(e)}')
                continue
            devices += list(map(lambda x: (name, x), u))
        return devices
    ####################################################
    def create(self, name, password, seed=None, passphrase="", bip39_derivation=None,
               master=None, addresses=None, privkeys=None):
        """Create or restore a new wallet"""
        print("CREATE in....name = %s" % name)
        is_exist = False
        path = self._wallet_path(name)
        if exists(path):
            is_exist = True
            return is_exist
            # raise FileExistsError(path)
        storage = WalletStorage(path)

        if addresses is not None:
            wallet = ImportedAddressWallet.from_text(storage, addresses)
        elif privkeys is not None:
            wallet = ImportedPrivkeyWallet.from_text(storage, privkeys)
        else:
            if bip39_derivation is not None:
                ks = keystore.from_bip39_seed(seed, passphrase, bip39_derivation)
            elif master is not None:
                ks = keystore.from_master_key(master)
            else:
                if seed is None:
                    seed = self.make_seed()
                    print("Your wallet generation seed is:\n\"%s\"" % seed)
                ks = keystore.from_seed(seed, passphrase, False)

            storage.put('keystore', ks.dump())
            wallet = Standard_Wallet(storage)

        wallet.update_password(None, password, True)
        return is_exist
    # END commands from the argparse interface.

    # BEGIN commands which only exist here.

    def select_wallet(self, name):
        if name is None:
            self.wallet = None
        else:
            self.wallet = self.daemon.wallets[self._wallet_path(name)]

        print("console.select_wallet[%s]=%s" %(name, self.wallet))
        c, u, x = self.wallet.get_balance()
        print("console.select_wallet %s %s %s==============" %(c, u, x))
        print("console.select_wallet[%s] blance = %s wallet_type = %s use_change=%s add = %s " %(name, self.format_amount_and_units(c), self.wallet.wallet_type,self.wallet.use_change, self.wallet.get_addresses()))
        self.network.trigger_callback("wallet_updated", self.wallet)

    def list_wallets(self):
        """List available wallets"""
        name_wallets = sorted([name for name in os.listdir(self._wallet_path())])
        print("console.list_wallets is %s................" %name_wallets)
        return name_wallets

    def delete_wallet(self, name=None):
        """Delete a wallet"""
        try:
            os.remove(self._wallet_path(name))
        except Exception as e:
            raise e

    def unit_test(self):
        """Run all unit tests. Expect failures with functionality not present on Android,
        such as Trezor.
        """
        suite = unittest.defaultTestLoader.loadTestsFromNames(
            tests.__name__ + "." + info.name
            for info in pkgutil.iter_modules(tests.__path__)
            if info.name.startswith("test_"))
        unittest.TextTestRunner(verbosity=2).run(suite)

    # END commands which only exist here.

    def _assert_daemon_running(self):
        if not self.daemon_running:
            raise Exception("Daemon not running")  # Same wording as in electrum script.

    def _assert_wizard_isvalid(self):
        if self.wizard is None:
            raise Exception("Wizard not running")
            # Log callbacks on stderr so they'll appear in the console activity.

    def _assert_wallet_isvalid(self):
        if self.wallet is None:
            raise Exception("Wallet is None")
            # Log callbacks on stderr so they'll appear in the console activity.

    def _on_callback(self, *args):
        util.print_stderr("[Callback] " + ", ".join(repr(x) for x in args))

    def _wallet_path(self, name=""):
        if name is None:
            if not self.wallet:
                raise ValueError("No wallet selected")
            return self.wallet.storage.path
        else:
            wallets_dir = join(util.user_dir(), "wallets")
            util.make_dir(wallets_dir)
            return util.standardize_path(join(wallets_dir, name))


all_commands = commands.known_commands.copy()
for name, func in vars(AndroidCommands).items():
    if not name.startswith("_"):
        all_commands[name] = commands.Command(func, "")


SP_SET_METHODS = {
    bool: "putBoolean",
    float: "putFloat",
    int: "putLong",
    str: "putString",
}

