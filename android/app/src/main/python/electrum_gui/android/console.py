from __future__ import absolute_import, division, print_function

import copy
from code import InteractiveConsole
import json
import os
from decimal import Decimal
from os.path import exists, join
import pkgutil
import unittest
import threading
from electrum.bitcoin import base_decode, is_address
from electrum.keystore import bip44_derivation
from electrum.plugin import Plugins
from electrum.plugins.trezor.clientbase import TrezorClientBase
from electrum.transaction import PartialTransaction, Transaction, TxOutput, PartialTxOutput, tx_from_any, TxInput, PartialTxInput
from electrum import commands, daemon, keystore, simple_config, storage, util, bitcoin
from electrum.util import Fiat, create_and_start_event_loop, decimal_point_to_base_unit_name
from electrum import MutiBase
from electrum.i18n import _
from electrum.storage import WalletStorage
from electrum.wallet import (Standard_Wallet,
                                 Wallet)
from electrum.bitcoin import is_address,  hash_160, COIN, TYPE_ADDRESS
from electrum import mnemonic
from electrum.address_synchronizer import TX_HEIGHT_LOCAL, TX_HEIGHT_FUTURE
#from android.preference import PreferenceManager
from electrum.commands import satoshis
from electrum.bip32 import BIP32Node, convert_bip32_path_to_list_of_uint32 as parse_path
from electrum.network import Network, TxBroadcastError, BestEffortRequestFailed
import trezorlib.btc
from electrum import ecc
from trezorlib.customer_ui import CustomerUI
from electrum.wallet_db import WalletDB
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
        loop, stop_loop, loop_thread = create_and_start_event_loop()#TODO:close loop
        config_options = {}
        config_options['auto_connect'] = True
        self.config = simple_config.SimpleConfig(config_options)
        fd = daemon.get_file_descriptor(self.config)
        if not fd:
            raise BaseException("Daemon already running")  # Same wording as in daemon.py.

        # Initialize here rather than in start() so the DaemonModel has a chance to register
        # its callback before the daemon threads start.
        self.daemon = daemon.Daemon(self.config, fd)
        self.network = self.daemon.network
        self.daemon_running = False
        self.wizard = None
        self.plugin = Plugins(self.config, 'cmdline')
        self.label_plugin = self.plugin.load_plugin("labels")
        self.label_flag = self.config.get('use_labels', False)
        self.callbackIntent = None
        self.wallet = None
        self.client = None
        self.path = ''
        self.local_wallet_info = self.config.get("all_wallet_type_info", {})
        if self.network:
            interests = ['wallet_updated', 'network_updated', 'blockchain_updated',
                         'status', 'new_transaction', 'verified']
            self.network.register_callback(self.on_network_event, interests)
            self.network.register_callback(self.on_fee, ['fee'])
            self.network.register_callback(self.on_fee_histogram, ['fee_histogram'])
            self.network.register_callback(self.on_quotes, ['on_quotes'])
            self.network.register_callback(self.on_history, ['on_history'])
        self.fiat_unit = self.daemon.fx.ccy if self.daemon.fx.is_enabled() else ''
        self.decimal_point = self.config.get('decimal_point', util.DECIMAL_POINT_DEFAULT)
        for k, v in util.base_units_inverse.items():
            if k == self.decimal_point:
                self.base_unit = v

        self.num_zeros = int(self.config.get('num_zeros', 0))
        self.config.set_key('log_to_file', True, save=True)
        self.rbf = self.config.get("use_rbf", True)
        self.ccy = self.daemon.fx.get_currency()
        self.m = 0
        self.n = 0
        self.config.set_key('auto_connect', True, True)
        from threading import Timer
        t = Timer(5.0, self.timer_action)
        t.start()

    # BEGIN commands from the argparse interface.
    def is_valiad_xpub(self, xpub):
        return keystore.is_bip32_key(xpub)

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
            print("network is ========offline")
            status = _("Offline")
        elif self.network.is_connected():
            #print("network is ========connect")
            out = {}
            #out['wallet_type'] = self.wallet.wallet_type
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
                c, u, x = self.wallet.get_balance()
                text = _("Balance") + ": %s " % (self.format_amount_and_units(c))
                out['balance'] = self.format_amount(c)
                out['fiat'] = self.daemon.fx.format_amount_and_units(c) if self.daemon.fx else None
                if u:
                    out['unconfirmed'] = self.format_amount(u, is_diff=True).strip()
                    text += " [%s unconfirmed]" % (self.format_amount(u, is_diff=True).strip())
                if x:
                    out['unmatured'] = self.format_amount(x, is_diff=True).strip()
                    text += " [%s unmatured]" % (self.format_amount(x, is_diff=True).strip())
                if self.wallet.lnworker:
                    l = self.wallet.lnworker.get_balance()
                    text += u'    \U0001f5f2 %s' % (self.format_amount_and_units(l).strip())

                # append fiat balance and price
                if self.daemon.fx.is_enabled():
                    text += self.daemon.fx.get_fiat_status_text(c + u + x, self.base_unit, self.decimal_point) or ''
            #print("update_statue out = %s" % (out))
        self.callbackIntent.onCallback("update_status", json.dumps(out))

    def get_remove_flag(self, tx_hash):
        height = self.wallet.get_tx_height(tx_hash).height
        if height in [TX_HEIGHT_FUTURE, TX_HEIGHT_LOCAL]:
            return True
        else:
            return False

    def remove_local_tx(self, delete_tx):
        to_delete = {delete_tx}
        to_delete |= self.wallet.get_depending_transactions(delete_tx)

        for tx in to_delete:
            self.wallet.remove_transaction(tx)
        self.wallet.storage.write()
        # need to update at least: history_list, utxo_list, address_list
        #self.parent.need_update.set()

    def save_tx_to_file(self, path, tx):
        print("save_tx_to_file in.....")
        with open(path, 'w') as f:
            f.write(tx)

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

    def timer_action(self):
        self.update_wallet()
        self.update_interfaces()

    def daemon_action(self):
        self.daemon_running = True
        self.daemon.run_daemon()

    def start(self):
        t1 = threading.Thread(target=self.daemon_action)
        t1.setDaemon(True)
        t1.start()
        import time
        time.sleep(1.0)
        print("parent thread")

    def status(self):
        """Get daemon status"""
        try:
            self._assert_daemon_running()
        except Exception as e:
            raise BaseException(e)
        return self.daemon.run_daemon({"subcommand": "status"})

    def stop(self):
        """Stop the daemon"""
        try:
            self._assert_daemon_running()
        except Exception as e:
            raise BaseException(e)
        self.daemon.stop()
        self.daemon.join()
        self.daemon_running = False

    def get_wallet_type(self, name):
        return self.local_wallet_info.get(name)

    def get_all_wallet_type_info(self):
        print("all type info == %s" % self.local_wallet_info)

    def load_wallet(self, name, password=None):
        """Load a wallet"""
        print("console.load_wallet_by_name in....")
        try:
            self._assert_daemon_running()
        except Exception as e:
            raise BaseException(e)
        path = self._wallet_path(name)
        wallet = self.daemon.get_wallet(path)
        if not wallet:
            storage = WalletStorage(path)
            if not storage.file_exists():
                raise BaseException("not find file %s" %path)
            if storage.is_encrypted():
                if not password:
                    raise BaseException(util.InvalidPassword())
                storage.decrypt(password)
            db = WalletDB(storage.read(), manual_upgrades=False)
            if db.requires_split():
                return
            if db.requires_upgrade():
                return
            if db.get_action():
                return
            wallet = Wallet(db, storage, config=self.config)
            wallet.start_network(self.network)
            self.daemon.add_wallet(wallet)

    def close_wallet(self, name=None):
        """Close a wallet"""
        try:
            self._assert_daemon_running()
        except Exception as e:
            raise BaseException(e)
        self.daemon.stop_wallet(self._wallet_path(name))

    ###syn server
    def set_syn_server(self, flag):
        self.label_flag = flag
        self.config.set_key('use_labels', bool(flag))

    ##set callback##############################
    def set_callback_fun(self, callbackIntent):
        print("self.callbackIntent =%s" %callbackIntent)
        self.callbackIntent = callbackIntent

    #craete multi wallet##############################
    def set_multi_wallet_info(self, name, m, n):
        try:
            self._assert_daemon_running()
        except Exception as e:
            raise BaseException(e)
        if self.wizard is not None:
            self.wizard = None
        self.wizard = MutiBase.MutiBase(self.config)
        path = self._wallet_path(name)
        print("console:set_multi_wallet_info:path = %s---------" % path)
        self.wizard.set_multi_wallet_info(path, m, n)
        self.m = m
        self.n = n

    def add_xpub(self, xpub):
        try:
            self._assert_daemon_running()
            self._assert_wizard_isvalid()
            self.wizard.restore_from_xpub(xpub)
        except Exception as e:
            raise BaseException(e)

    def delete_xpub(self, xpub):
        try:
            self._assert_daemon_running()
            self._assert_wizard_isvalid()
            self.wizard.delete_xpub(xpub)
        except Exception as e:
            raise BaseException(e)

    def get_keystores_info(self):
        try:
            self._assert_daemon_running()
            self._assert_wizard_isvalid()
            ret = self.wizard.get_keystores_info()
        except Exception as e:
            raise BaseException(e)
        return ret

    def get_cosigner_num(self):
        try:
            self._assert_daemon_running()
            self._assert_wizard_isvalid()
        except Exception as e:
            raise BaseException(e)
        return self.wizard.get_cosigner_num()

    def create_multi_wallet(self, name):
        try:
            self._assert_daemon_running()
            self._assert_wizard_isvalid()
            path = self._wallet_path(name)
            print("console:create_multi_wallet:path = %s---------" % path)
            storage, db = self.wizard.create_storage(path=path, password = '')
        except Exception as e:
            raise BaseException(e)
        if storage:
            wallet = Wallet(db, storage, config=self.config)
            wallet.start_network(self.daemon.network)
            self.daemon.add_wallet(wallet)
            self.local_wallet_info[name] = ("%s-%s" % (self.m, self.n))
            self.config.set_key('all_wallet_type_info', self.local_wallet_info)
            if self.wallet:
                self.close_wallet()
            self.wallet = wallet
            self.wallet_name = wallet.basename()
            print("console:create_multi_wallet:wallet_name = %s---------" % self.wallet_name)
            self.select_wallet(self.wallet_name)
        self.wizard = None

    ##create tx#########################
    def get_default_fee_status(self):
        return self.config.get_fee_status()

    def get_amount(self, amount):
        try:
            x = Decimal(str(amount))
        except:
            return None
        # scale it to max allowed precision, make it an int
        power = pow(10, self.decimal_point)
        max_prec_amount = int(power * x)
        return max_prec_amount

    def parse_output(self, outputs):
        all_output_add = json.loads(outputs)
        outputs_addrs = []
        for key in all_output_add:
            for address, amount in key.items():
                outputs_addrs.append(PartialTxOutput.from_address_and_value(address, self.get_amount(amount)))
                print("console.mktx[%s] wallet_type = %s use_change=%s add = %s" % (
                    self.wallet, self.wallet.wallet_type, self.wallet.use_change, self.wallet.get_addresses()))
        return outputs_addrs

    def get_fee_by_feerate(self, outputs, message, feerate):
        try:
            self._assert_wallet_isvalid()
            all_output_add = json.loads(outputs)
            outputs_addrs = self.parse_output(outputs)
            coins = self.wallet.get_spendable_coins(domain=None)
            fee_per_kb = 1000 * Decimal(feerate)
            from functools import partial
            fee_estimator = partial(self.config.estimate_fee_for_feerate, fee_per_kb)
            # tx = self.wallet.make_unsigned_transaction(coins=coins, outputs = outputs_addrs, fee=self.get_amount(fee_estimator))
            tx = self.wallet.make_unsigned_transaction(coins=coins, outputs=outputs_addrs, fee=fee_estimator)
            tx.set_rbf(self.rbf)
            self.wallet.set_label(tx.txid(), message)
            size = tx.estimated_size()
            fee = tx.get_fee()
            print("feee-----%s size =%s" % (fee, size))
            self.tx = tx.serialize_as_bytes().hex()
            print("console:mkun:tx====%s" % self.tx)
            tx_details = self.wallet.get_tx_info(tx)
            print("tx_details 1111111 = %s" % json.dumps(tx_details))

            ret_data = {
                'amount': tx_details.amount,
                'fee': tx_details.fee,
                'tx': str(self.tx)
            }
            return json.dumps(ret_data)
        except Exception as e:
            raise BaseException(e)

    def mktx(self, outputs, message):
        try:
            self._assert_wallet_isvalid()
            outputs_addrs = self.parse_output(outputs)
            #self.do_save(outputs_addrs, message, self.tx)
            tx = tx_from_any(self.tx)
            tx.deserialize()
            self.do_save(tx)
            if self.label_flag:
                self.label_plugin.push(self.wallet)
        except Exception as e:
            raise BaseException(e)

        ret_data = {
            'tx': str(self.tx)
        }
        json_str = json.dumps(ret_data)
        return json_str

    def deserialize(self, raw_tx):
        try:
            tx = Transaction(raw_tx)
            tx.deserialize()
        except Exception as e:
            raise BaseException(e)

    def get_wallets_list_info(self):
        try:
            self._assert_daemon_running()
        except Exception as e:
            raise BaseException(e)
        wallet_info_map = {}
        wallets = self.list_wallets()
        wallet_info = []
        for i in wallets:
            print("---------name=%s" %i)
            self.load_wallet(i, '111111')
            data = self.select_wallet(i)


            # path = self._wallet_path(i)
            # wallet = self.daemon.get_wallet(path)
            # print("--------name = %s" % i)
            # if not wallet:
            #     storage = WalletStorage(path)
            #     if not storage.file_exists():
            #         raise BaseException("not find file %s" %path)
            #
            #     try:
            #         #wallet = Standard_Wallet(storage, config=self.config)
            #         wallet = Wallet(storage, config=self.config)
            #     except Exception as e:
            #        # wallet = Standard_Wallet(storage, config=self.config)
            #         raise BaseException(e)
            # c, u, x = wallet.get_balance()
            # info = {
            #     "wallet_type": wallet.wallet_type,
            #     "balance": self.format_amount_and_units(c),
            #     "name": i
            # }
            wallet_info.append(json.loads(data))
            wallet_info_map['wallets'] = wallet_info
            print("----wallet_info = %s ............" % wallet_info_map)
        return json.dumps(wallet_info_map)

    def format_amount(self, x, is_diff=False, whitespaces=False):
        return util.format_satoshis(x, self.num_zeros, self.decimal_point, is_diff=is_diff, whitespaces=whitespaces)

    def base_unit(self):
        return util.decimal_point_to_base_unit_name(self.decimal_point)

    #set use unconfirmed coin
    def set_unconf(self, x):
        self.config.set_key('confirmed_only', bool(x))

    #fiat balance
    def get_currencies(self):
        self._assert_daemon_running()
        currencies = sorted(self.daemon.fx.get_currencies(self.daemon.fx.get_history_config()))
        return currencies

    def get_exchanges(self):
        if not self.daemon.fx: return
        b = self.daemon.fx.is_enabled()
        if b:
            h = self.daemon.fx.get_history_config()
            c = self.daemon.fx.get_currency()
            exchanges = self.daemon.fx.get_exchanges_by_ccy(c, h)
        else:
            exchanges = self.daemon.fx.get_exchanges_by_ccy('USD', False)
        return sorted(exchanges)

    def set_exchange(self, exchange):
        if self.daemon.fx and self.daemon.fx.is_enabled() and exchange and exchange != self.daemon.fx.exchange.name():
            self.daemon.fx.set_exchange(exchange)

    def set_currency(self, ccy):
        self.daemon.fx.set_enabled(True)
        if ccy != self.ccy:
            self.daemon.fx.set_currency(ccy)
            self.ccy = ccy
        self.update_status()

    def get_exchange_currency(self, type, amount):
        text = ""
        rate = self.daemon.fx.exchange_rate() if self.daemon.fx else Decimal('NaN')
        if rate.is_nan() or amount is None:
            return text
        else:
            if type == "base":
                amount = self.get_amount(amount)
                text = self.daemon.fx.ccy_amount_str(amount * Decimal(rate) / COIN, False)
            elif type == "fiat":
                text = self.format_amount((int(Decimal(amount) / Decimal(rate) * COIN)))
            return text

    #set base unit for(BTC/mBTC/bits/sat)
    def set_base_uint(self, base_unit):
        self.base_unit = base_unit
        self.decimal_point = util.base_unit_name_to_decimal_point(self.base_unit)
        self.config.set_key('decimal_point', self.decimal_point, True)

    def format_amount_and_units(self, amount):
        try:
            self._assert_daemon_running()
        except Exception as e:
            raise BaseException(e)
        text = self.format_amount(amount) + ' '+ self.base_unit
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

    def recover_tx_info(self, tx):
        tx = tx_from_any(bytes.fromhex(tx))
        temp_tx = copy.deepcopy(tx)
        temp_tx.deserialize()
        temp_tx.add_info_from_wallet(self.wallet)
        return temp_tx

    ## get tx info from raw_tx
    def get_tx_info_from_raw(self, raw_tx):
        try:
            print("console:get_tx_info_from_raw:tx===%s" % raw_tx)
            tx = self.recover_tx_info(raw_tx)
        except Exception as e:
            tx = None
            raise BaseException(e)
        data = {}
        # if not isinstance(tx, PartialTransaction):
        #     tx = PartialTransaction.from_tx(tx)
        data = self.get_details_info(tx)
        return data

    def get_details_info(self, tx):
        try:
            self._assert_wallet_isvalid()
        except Exception as e:
            raise BaseException(e)
        tx_details = self.wallet.get_tx_info(tx)
        if 'Partially signed' in tx_details.status:
            r = len(self.wallet.get_keystores())
            temp_s, temp_r = tx.signature_count()
            s, r = temp_s/r, temp_r/r
        elif 'Unsigned' in tx_details.status:
            s = 0
            r = len(self.wallet.get_keystores())
        else:
            s = r = len(self.wallet.get_keystores())

        type = self.wallet.wallet_type
        in_list = []
        if isinstance(tx, PartialTransaction):
            for i in tx.inputs():
                in_info = {}
                in_info['addr'] = i.address
                in_list.append(in_info)

        out_list = []
        for o in tx.outputs():
            address, value = o.address, o.value
            out_info = {}
            out_info['addr'] = address
            out_info['amount'] = self.format_amount_and_units(value)
            out_list.append(out_info)
        #print("===============tx:%s" %tx.serialize_as_bytes().hex())
        ret_data = {
            'txid':tx_details.txid,
            'can_broadcast':tx_details.can_broadcast,
            'amount': self.format_amount_and_units(tx_details.amount),
            'fee': self.format_amount_and_units(tx_details.fee) if isinstance(tx, PartialTransaction) else 0,
            'description':self.wallet.get_label(tx_details.txid),
            'tx_status':tx_details.status,#TODO:需要对应界面的几个状态
            'sign_status':[s,r],
            'output_addr':out_list,
            'input_addr':in_list,
            'cosigner':[x.xpub for x in self.wallet.get_keystores()],
            'tx':tx.serialize_as_bytes().hex()
        }
        json_data = json.dumps(ret_data)
        return json_data

    #invoices
    def delete_invoice(self, key):
        try:
            self._assert_wallet_isvalid()
            self.wallet.delete_invoice(key)
        except Exception as e:
            raise BaseException(e)

    def get_invoices(self):
        try:
            self._assert_wallet_isvalid()
            return self.wallet.get_invoices()
        except Exception as e:
            raise BaseException(e)

    #def do_save(self, outputs, message, tx):
        # try:
        #     invoice = self.wallet.create_invoice(outputs, message, None, None, tx=tx)
        #     if not invoice:
        #         return
        #     self.wallet.save_invoice(invoice)
        # except Exception as e:
        #     raise BaseException(e)

    def do_save(self, tx):
        try:
            if not self.wallet.add_transaction(tx):
                raise BaseException(("Transaction could not be saved.") + "\n" + ("It conflicts with current history. tx=") + tx.txid())
        except BaseException as e:
            raise BaseException(e)
        else:
            self.wallet.save_db()
            self.callbackIntent.onCallback("update_history", "update history")
            # need to update at least: history_list, utxo_list, address_list

    def update_invoices(self, old_tx, new_tx):
        try:
            self._assert_wallet_isvalid()
            self.wallet.update_invoice(old_tx, new_tx)
        except Exception as e:
            raise BaseException(e)

    def clear_invoices(self):
        try:
            self._assert_wallet_isvalid()
            self.wallet.clear_invoices()
        except Exception as e:
            raise BaseException(e)

    ##get history
    def get_all_tx_list(self, search_type=None):
        history_data = []
        history_info = self.get_history_tx()
        history_dict = json.loads(history_info)
        if search_type is None:
            history_data = history_dict
        elif search_type == 'send':
            for info in history_dict:
                if info['is_mine']:
                    history_data.append(info)
        elif search_type == 'receive':
            for info in history_dict:
                if not info['is_mine']:
                    history_data.append(info)

        all_data = []
        for i in history_data:
            i['type'] = 'history'
            data = self.get_tx_info(i['tx_hash'])
            i['tx_status'] = json.loads(data)['tx_status']
            all_data.append(i)
        return json.dumps(all_data)

    def get_all_tx_list_old(self, tx_status=None, history_status=None):#tobe optimization
        tx_data = []
        history_data = []
        if (tx_status is None or tx_status == 'send') and (history_status == None or history_status == 'tobesign' or history_status == 'tobebroadcast'):
            invoices = self.wallet.get_invoices()
            for invoice in invoices:
                tx_json = self.get_tx_info_from_raw(invoice['tx'])
                tx_dict = json.loads(tx_json)
                temp_tx_data = {}
                temp_tx_data['tx_hash'] = tx_dict['txid']
                temp_tx_data['date'] = util.format_time(invoice['time']) if invoice['time'] else _("unknown")
                temp_tx_data['amount'] = tx_dict['amount']
                print("amount------%s fee======%s temp_tx_data=%s" %(tx_dict['amount'], tx_dict['fee'],temp_tx_data['amount']))
                temp_tx_data['message'] = tx_dict['description']
                temp_tx_data['is_mine'] = True
                temp_tx_data['tx_status'] = tx_dict["tx_status"]
                temp_tx_data['invoice_id'] = invoice['id']
                temp_tx_data['tx'] = invoice['tx']
                print("invoice====%s type=%s" % (invoice['tx'], type(invoice['tx'])))
                tx_data.append(temp_tx_data)

        if history_status is None and tx_status is None:
            history_data_json = self.get_history_tx()
            history_data = json.loads(history_data_json)
        elif tx_status is None and history_status is not None:
            if history_status == 'tobeconfirm':
                print("")
                history_info = self.get_history_tx()
                history_dict = json.loads(history_info)
                for info in history_dict:
                    con_num = info['confirmations']
                    if info['confirmations'] <= 0:
                        history_data.append(info)
            elif history_status == 'confirmed':
                print("")
                history_info = self.get_history_tx()
                history_dict = json.loads(history_info)
                for info in history_dict:
                    if info['confirmations'] > 0:
                        history_data.append(info)
        elif history_status is None and tx_status is not None:
            if tx_status == 'send':
                print("")
                history_info = self.get_history_tx()
                history_dict = json.loads(history_info)
                for info in history_dict:
                    if info['is_mine']:
                        history_data.append(info)
            elif tx_status == 'receive':
                print("")
                history_info = self.get_history_tx()
                history_dict = json.loads(history_info)
                for info in history_dict:
                    if not info['is_mine']:
                        history_data.append(info)
        elif tx_status == 'send':
            if history_status == 'tobeconfirm':
                history_info = self.get_history_tx()
                history_dict = json.loads(history_info)
                for info in history_dict:
                    if info['is_mine'] and info['confirmations'] <= 0:
                        history_data.append(info)
            elif history_status == 'confirmed':
                history_info = self.get_history_tx()
                history_dict = json.loads(history_info)
                for info in history_dict:
                    if info['is_mine'] and info['confirmations'] > 0:
                        history_data.append(info)
        elif tx_status == 'receive':
            if history_status == 'tobeconfirm':
                history_info = self.get_history_tx()
                history_dict = json.loads(history_info)
                for info in history_dict:
                    if not info['is_mine'] and info['confirmations'] <= 0:
                        history_data.append(info)
            elif history_status == 'confirmed':
                history_info = self.get_history_tx()
                history_dict = json.loads(history_info)
                for info in history_dict:
                    if not info['is_mine'] and info['confirmations'] > 0:
                        history_data.append(info)
        all_data = []
        for tx in tx_data:
            tx['type'] = 'tx'
            all_data.append(tx)
        for i in history_data:
            i['type'] = 'history'
            all_data.append(i)
        return json.dumps(all_data)

    ##history
    def get_history_tx(self):
        try:
            self._assert_wallet_isvalid()
        except Exception as e:
            raise BaseException(e)
        history = reversed(self.wallet.get_history())
        all_data = [self.get_card(*item) for item in history]
        return json.dumps(all_data)

    def get_tx_info(self, tx_hash):
        try:
            self._assert_wallet_isvalid()
        except Exception as e:
            raise BaseException(e)
        tx = self.wallet.db.get_transaction(tx_hash)
       # print("get_tx_info:tx=%s" % tx)
        if not tx:
            raise BaseException('get transaction info failed')
        #tx = PartialTransaction.from_tx(tx)
        label = self.wallet.get_label(tx_hash) or None
        tx = copy.deepcopy(tx)
        try:
            tx.deserialize()
        except Exception as e:
            raise e
        tx.add_info_from_wallet(self.wallet)
        return self.get_details_info(tx)

    def get_card(self, tx_hash, tx_mined_status, delta, fee, balance):
        try:
            self._assert_wallet_isvalid()
            self._assert_daemon_running()
        except Exception as e:
            raise BaseException(e)
        status, status_str = self.wallet.get_tx_status(tx_hash, tx_mined_status)
        label = self.wallet.get_label(tx_hash) if tx_hash else _('Pruned transaction outputs')
        ri = {}
        ri['tx_hash'] = tx_hash
        ri['date'] = status_str
        ri['message'] = label
        ri['confirmations'] = tx_mined_status.conf
        if delta is not None:
            ri['is_mine'] = delta < 0
            if delta < 0: delta = - delta
            ri['amount'] = self.format_amount_and_units(delta)
            if self.fiat_unit:
                fx = self.daemon.fx
                fiat_value = delta / Decimal(bitcoin.COIN) * self.wallet.price_at_timestamp(tx_hash, fx.timestamp_rate)
                fiat_value = Fiat(fiat_value, fx.ccy)
                ri['quote_text'] = fiat_value.to_ui_string()
        return ri

    def get_wallet_address_show_UI(self):#TODO:需要按照electrum方式封装二维码数据?
        try:
            self._assert_wallet_isvalid()
            data = util.create_bip21_uri(self.wallet.get_addresses()[0], "", "")
        except Exception as e:
            raise BaseException(e)
        data_json = {}
        data_json['qr_data'] = data
        data_json['addr'] = self.wallet.get_addresses()[0]
        return json.dumps(data_json)

    ##save tx to file
    def save_tx_to_file(self, path, tx):
        print("FILE==:save_tx_to_file in..... %s" % path)
        try:
            if tx is None:
                raise BaseException("tx is empty")
            tx = tx_from_any(tx)
            if isinstance(tx, PartialTransaction):
                tx.finalize_psbt()
            print("FILE==:path = %s" % path)
            if tx.is_complete():  # network tx hex
                with open(path, "w+") as f:
                    network_tx_hex = tx.serialize_to_network()
                    print("FILE==:TXN")
                    f.write(network_tx_hex + '\n')
            else:  # if partial: PSBT bytes
                assert isinstance(tx, PartialTransaction)
                with open(path, "wb+") as f:
                    print("FILE==:PSBT")
                    f.write(tx.serialize_as_bytes())
        except Exception as e:
            raise BaseException(e)

    def read_tx_from_file(self, path):
        try:
            with open(path, "rb") as f:
                file_content = f.read()
        except (ValueError, IOError, os.error) as reason:
            raise BaseException("Electrum was unable to open your transaction file")
        print("FILE== file info = %s" % file_content)
        tx = tx_from_any(file_content)
        return tx.serialize_as_bytes().hex()

    ##Analyze QR data
    def parse_address(self, data):
        data = data.strip()
        try:
            uri = util.parse_URI(data)
            uri['amount'] = self.format_amount_and_units(uri['amount'])
            return uri
        except Exception as e:
            raise Exception(e)

    def parse_tx(self, data):
        ret_data = {}
        # try to decode transaction
        from electrum.transaction import Transaction
        from electrum.util import bh2u
        try:
            text = bh2u(base_decode(data, base=43))
            tx = self.recover_tx_info(text)
        except Exception as e:
            tx = None
            raise Exception(e)
        # if tx:
        #     if not isinstance(tx, PartialTransaction):
        #         tx = PartialTransaction.from_tx(tx)

        data = self.get_details_info(tx)
        return data

    def parse_pr(self, data):
        add_status_flag = False
        tx_status_flag = False
        try:
            add_data = self.parse_address(data)
            add_status_flag = True
        except Exception as e:
            print("parse_pr...............error address")
            add_status_flag = False

        try:
            tx_data = self.parse_tx(data)
            tx_status_flag = True
        except Exception as e:
            print("parse_pr...............error tx")
            tx_status_flag = False
        out_data = {}
        if(add_status_flag):
            out_data['type'] = 1
            out_data['data'] = add_data
        elif(tx_status_flag):
            out_data['type'] = 2
            out_data['data'] = json.loads(tx_data)
        else:
            out_data['type'] = 3
            out_data['data'] = "parse pr error"
        return json.dumps(out_data)

    def broadcast_tx(self, tx):
        if self.network and self.network.is_connected():
            status = False
            try:
                if isinstance(tx, str):
                    tx = tx_from_any(tx)
                    tx.deserialize()
                self.network.run_from_another_thread(self.network.broadcast_transaction(tx))
            except TxBroadcastError as e:
                msg = e.get_message_for_gui()
                raise BaseException(msg)
            except BestEffortRequestFailed as e:
                msg = repr(e)
                raise BaseException(msg)
            else:
                print("--------broadcast ok............")
                status, msg = True, tx.txid()
      #          self.callbackIntent.onCallback(Status.broadcast, msg)
        else:
            raise BaseException(('Cannot broadcast transaction') + ':\n' + ('Not connected'))

    ## setting
    def set_use_change(self, status_change):
        try:
            self._assert_wallet_isvalid()
        except Exception as e:
            raise BaseException(e)
        if self.wallet.use_change == status_change:
            return
        self.config.set_key('use_change', status_change, False)
        self.wallet.use_change = status_change

    def sign_tx(self, tx) -> str:
        try:
            self._assert_wallet_isvalid()
            ptx = tx_from_any(bytes.fromhex(tx))
            stx = self.wallet.sign_transaction(ptx, None)
            self.do_save(stx)
            raw_tx = stx.serialize_as_bytes().hex()
            return self.get_tx_info_from_raw(raw_tx)
        except Exception as e:
            raise BaseException(e)

    ##connection with terzorlib#########################
    def backup_wallet(self):
        return "hello world"

    def wallet_recovery(self, str):
        if str == "hello world":
            return True
        else:
            return False

    def init(self):
        client = self.get_client()
        try:
            response = client.reset_device()
        except Exception as e:
            raise BaseException(e)
        CustomerUI.state = 1
        return response

    def reset_pin(self):
        client = self.get_client()
        client.set_pin(True)

    def get_client(self, path='nfc', ui=CustomerUI()) -> 'TrezorClientBase':
        if self.client is not None and self.path == path:
            return self.client
        plugin = self.plugin.get_plugin("trezor")
        client_list = plugin.enumerate()
        print(client_list)
        device = [cli for cli in client_list if cli.path == path]
        assert len(device) != 0, "Not found the point device"
        client = plugin.create_client(device[0], ui)
        self.client = client
        self.path = path
        return client

    # def get_feature(self):
    #     client = self.get_client()
    #     return client.features

    def is_initialized(self, path='nfc'):
        client = self.get_client(path=path)
        return client.is_initialized()

    def get_pin_status(self, path='nfc'):
        self.client = None
        self.path = ''
        client = self.get_client(path=path)
        return client.features.pin_cached

    def get_xpub_from_hw(self):
        client = self.get_client()
        derivation = bip44_derivation(0)
        try:
            xpub = client.get_xpub(derivation, 'p2wsh')
        except Exception as e:
            raise BaseException(e)
        return xpub

    ####################################################
    ## app wallet
    def check_seed(self, check_seed, password):
        try:
            self._assert_wallet_isvalid()
            if not self.wallet.has_seed():
                raise BaseException('This wallet has no seed')
            keystore = self.wallet.get_keystore()
            seed = keystore.get_seed(password)
            if seed != check_seed:
                raise BaseException("pair seed failed")
            print("pair seed successfule.....")
        except BaseException as e:
            raise BaseException(e)

    def create(self, name, password, seed=None, passphrase="", bip39_derivation=None,
               master=None, addresses=None, privkeys=None):
        """Create or restore a new wallet"""
        print("CREATE in....name = %s" % name)
        new_seed = ""
        path = self._wallet_path(name)
        if exists(path):
            raise BaseException("path is exist")
        storage = WalletStorage(path)
        db = WalletDB('', manual_upgrades=False)
        if addresses is not None:
            print("")
           # wallet = ImportedAddressWallet.from_text(storage, addresses)
        elif privkeys is not None:
            print("")
            #wallet = ImportedPrivkeyWallet.from_text(storage, privkeys)
        else:
            if bip39_derivation is not None:
                ks = keystore.from_bip39_seed(seed, passphrase, bip39_derivation)
            elif master is not None:
                ks = keystore.from_master_key(master)
            else:
                if seed is None:
                    seed = mnemonic.Mnemonic('en').make_seed(seed_type='segwit')
                    new_seed = seed
                    print("Your wallet generation seed is:\n\"%s\"" % seed)
                    print("seed type = %s" %type(seed))
                ks = keystore.from_seed(seed, passphrase, False)

            db.put('keystore', ks.dump())
            #db.put('wallet_type', 'standard')
            wallet = Standard_Wallet(db, storage, config=self.config)
            #wallet = Wallet(db, storage, config=self.config)
        wallet.update_password(old_pw=None, new_pw=password, encrypt_storage=True)
        wallet.start_network(self.daemon.network)
        wallet.save_db()
        self.daemon.add_wallet(wallet)
        self.local_wallet_info[name] = 'standard'
        self.config.set_key('all_wallet_type_info', self.local_wallet_info)
        return new_seed
    # END commands from the argparse interface.

    # BEGIN commands which only exist here.
    #####
    #rbf api
    def set_rbf(self, status_rbf):
        use_rbf = self.config.get('use_rbf', True)
        if use_rbf == status_rbf:
            return
        self.config.set_key('use_rbf', status_rbf)
        self.rbf = status_rbf

    def get_rbf_status(self, tx_hash):
        try:
            tx = self.wallet.db.get_transaction(tx_hash)
            if not tx:
                return False
            height = self.wallet.get_tx_height(tx_hash).height
            is_relevant, is_mine, v, fee = self.wallet.get_wallet_delta(tx)
            is_unconfirmed = height <= 0
            if tx:
                # note: the current implementation of RBF *needs* the old tx fee
                rbf = is_mine and self.rbf and fee is not None
                if rbf:
                    return True
                else:
                    return False
        except BaseException as e:
            raise e

    def format_fee_rate(self, fee_rate):
        # fee_rate is in sat/kB
        return util.format_fee_satoshis(fee_rate/1000, num_zeros=self.num_zeros) + ' sat/byte'

    def get_rbf_fee_info(self, tx_hash):
        tx = self.wallet.db.get_transaction(tx_hash)
        if not tx:
            return False
        # tx_data = json.loads(self.get_details_info(tx))
        # fee = tx_data['fee'].split(" ")[0]
        # fee = self.get_amount(fee)
        txid = tx.txid()
        assert txid
        fee = self.wallet.get_tx_fee(txid)
        if fee is None:
            raise BaseException("Can't bump fee: unknown fee for original transaction.")
        tx_size = tx.estimated_size()
        old_fee_rate = fee / tx_size  # sat/vbyte
        #data = max(old_fee_rate * 1.5, old_fee_rate + 1)
        new_rate = Decimal(max(old_fee_rate * 1.5, old_fee_rate + 1)).quantize(Decimal('0.0'))
        ret_data = {
            #'current_fee': self.format_amount(fee) + ' ' + self.base_unit(),
            'current_feerate': self.format_fee_rate(1000 * old_fee_rate),
            'new_feerate': str(new_rate),
        }
        return json.dumps(ret_data)

    #TODO:new_tx in history or invoices, need test

    def create_bump_fee(self, tx_hash, new_fee_rate, is_final):
        try:
            print("create bump fee tx_hash---------=%s" %tx_hash)
            tx = self.wallet.db.get_transaction(tx_hash)
            if not tx:
                return False
            coins = self.wallet.get_spendable_coins(None, nonlocal_only=False)
            new_tx = self.wallet.bump_fee(tx=tx, new_fee_rate=new_fee_rate, coins=coins)
        except BaseException as e:
            raise BaseException(e)

        new_tx.set_rbf(self.rbf)
        # if is_final:
        #     new_tx.set_rbf(False)
        out = {
            'new_tx': new_tx.serialize_as_bytes().hex()
        }
        self.do_save(new_tx)
        #self.update_invoices(tx, new_tx.serialize_as_bytes().hex())
        return json.dumps(out)
    #######

    #network server
    def get_default_server(self):
        try:
            self._assert_daemon_running()
            net_params = self.network.get_parameters()
            host, port, protocol = net_params.host, net_params.port, net_params.protocol
        except BaseException as e:
            raise e

        default_server = {
            'host':host,
            'port':port,
        }
        return json.dumps(default_server)

    def set_server(self, host, port):
        try:
            self._assert_daemon_running()
            net_params = self.network.get_parameters()
            net_params = net_params._replace(host=str(host),
                                         port=str(port),
                                         auto_connect=True)
            self.network.run_from_another_thread(self.network.set_parameters(net_params))
        except BaseException as e:
            raise e

    def get_server_list(self):
        try:
            self._assert_daemon_running()
            servers = self.daemon.network.get_servers()
        except BaseException as e:
            raise e
        return json.dumps(servers)

    def select_wallet(self, name):
        try:
            self._assert_daemon_running()
            if name is None:
                self.wallet = None
            else:
                self.wallet = self.daemon._wallets[self._wallet_path(name)]
            if self.label_flag:
                self.label_plugin.load_wallet(self.wallet, None)

            self.wallet.use_change = self.config.get('use_change', False)
            import time
            time.sleep(0.5)
            self.update_wallet()
            self.update_interfaces()

            c, u, x = self.wallet.get_balance()
            print("console.select_wallet %s %s %s==============" %(c, u, x))
            print("console.select_wallet[%s] blance = %s wallet_type = %s use_change=%s add = %s " %(name, self.format_amount_and_units(c), self.wallet.wallet_type,self.wallet.use_change, self.wallet.get_addresses()))
            self.network.trigger_callback("wallet_updated", self.wallet)
            info = {
                #"wallet_type": self.wallet.wallet_type,
                "balance": self.format_amount_and_units(c),
                "name": name
            }
            return json.dumps(info)
        except BaseException as e:
            raise BaseException(e)

    def list_wallets(self):
        """List available wallets"""
        name_wallets = sorted([name for name in os.listdir(self._wallet_path())])
        print("console.list_wallets is %s................" %name_wallets)
        out = []
        for name in name_wallets:
            name_info = {}
            name_info[name] = self.local_wallet_info.get(name) if self.local_wallet_info.__contains__(name) else 'unknow'
            out.append(name_info)
        return json.dumps(out)

    def delete_wallet(self, name=None):
        """Delete a wallet"""
        try:
            r = self.daemon.delete_wallet(self._wallet_path(name))
            self.local_wallet_info.pop(name)
            self.config.set_key('all_wallet_type_info', self.local_wallet_info)
            #os.remove(self._wallet_path(name))
        except Exception as e:
            raise BaseException(e)

    # def unit_test(self):
    #     """Run all unit tests. Expect failures with functionality not present on Android,
    #     such as Trezor.
    #     """
    #     suite = unittest.defaultTestLoader.loadTestsFromNames(
    #         tests.__name__ + "." + info.name
    #         for info in pkgutil.iter_modules(tests.__path__)
    #         if info.name.startswith("test_"))
    #     unittest.TextTestRunner(verbosity=2).run(suite)

    # END commands which only exist here.

    def _assert_daemon_running(self):
        if not self.daemon_running:
            raise BaseException("Daemon not running")  # Same wording as in electrum script.

    def _assert_wizard_isvalid(self):
        if self.wizard is None:
            raise BaseException("Wizard not running")
            # Log callbacks on stderr so they'll appear in the console activity.

    def _assert_wallet_isvalid(self):
        if self.wallet is None:
            raise BaseException("Wallet is None")
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

