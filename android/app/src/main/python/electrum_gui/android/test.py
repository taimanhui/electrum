from code import InteractiveConsole
import json
import os
from os.path import exists, join
import pkgutil
import unittest
from electrum.logging import get_logger, configure_logging
from electrum import util
from electrum import constants
from electrum import SimpleConfig
from electrum.wallet import Wallet
from electrum.storage import WalletStorage, get_derivation_used_for_hw_device_encryption
from electrum.util import print_msg, print_stderr, json_encode, json_decode, UserCancelled
from electrum.util import InvalidPassword
from electrum.commands import get_parser, known_commands, Commands, config_variables
from electrum import daemon
from electrum import keystore

_logger = get_logger(__name__)


from console import AndroidCommands

util.setup_thread_excepthook()

config_options = {}
config_options['cmdname'] = 'daemon'
config_options['testnet'] = True
config_options['cwd'] = os.getcwd()
config_options['auto_connect'] = True

# is_bundle = getattr(sys, 'frozen', False)
# # fixme: this can probably be achieved with a runtime hook (pyinstaller)
# if is_bundle and os.path.exists(os.path.join(sys._MEIPASS, 'is_portable')):
#     config_options['portable'] = True
#
# # if config_options.get('portable'):
#     config_options['electrum_path'] = os.path.join(os.path.dirname(os.path.realpath(__file__)), 'electrum_data')
#
# if not config_options.get('verbosity'):
#     warnings.simplefilter('ignore', DeprecationWarning)
# if not config_options.get('verbosity'):
#     warnings.simplefilter('ignore', DeprecationWarning)
#
# check uri
uri = config_options.get('url')
if uri:
    if not uri.startswith('bitcoin:'):
        print_stderr('unknown command:', uri)
        sys.exit(1)
    config_options['url'] = uri

# todo: defer this to gui
config = SimpleConfig(config_options)
cmdname = config.get('cmd')
print("cmdname = %s++++" %cmdname)

constants.set_testnet()

testcommond = AndroidCommands(config)
testcommond.start()

name = 'hahahahhahh222'
password = '111111'

#create_wallet

# m = 2
# n = 2
# xpub1 ="Vpub5gLTnhnQig7SLNhWCqE2AHqt8zhJGQwuwEAKQE67bndddSzUMAmab7DxZF9b9wynVyY2URM61SWY67QYaPV6oQrB41vMKQbeHveRvuThAmm"
# xpub2 ="Vpub5gyCX33B53xAyfEaH1Jfnp5grizbHfxVz6bWLPD92nLcbKMsQzSbM2eyGiK4qiRziuoRhoeVMoPLvEdfbQxGp88PN9cU6zupSSuiPi3RjEg"
# #testcommond.delete_wallet(name)
# testcommond.set_multi_wallet_info(name,m,n)
# testcommond.add_xpub(xpub1)
# testcommond.add_xpub(xpub2)
# testcommond.create_multi_wallet(name)

#load_wallet
testcommond.load_wallet(name, password)
testcommond.select_wallet(name)
info = testcommond.get_wallets_list_info()

#create_tx
all_output = []
output_info = ['tb1qwz3zcty8txqw077mckv5wycf2tj697ncnjwp9m', '0.1']
all_output.append(output_info)
output_str = join.dumps(all_output)
fee = 0.001
message = 'test'
ret_str = testcommond.mktx(output_str, message, fee)
ret_list = ret_str.loads()
print("tx================%s" % ret_list[tx])

#get_tx_by_raw
tx_info_str = get_tx_info_from_raw(ret_list[tx])
tx_info = tx_info_str.load()
print("tx info = %s=========" % tx_info)
#get_history_tx

#get_tx_info
