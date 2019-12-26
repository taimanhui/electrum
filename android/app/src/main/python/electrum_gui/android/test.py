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
from electrum.util import print_msg, print_stderr, json_encode, json_decode, UserCancelled, create_and_start_event_loop
from electrum.util import InvalidPassword
from electrum.commands import get_parser, known_commands, Commands, config_variables
from electrum import daemon
from electrum import keystore

_logger = get_logger(__name__)
from console import AndroidCommands

util.setup_thread_excepthook()
constants.set_testnet()

testcommond = AndroidCommands()
testcommond.start()

name = 'hahahahhahh777'
password = '111111'

#test hardware
#testcommond.get_xpub_from_hw()

#create_wallet

# m = 2
# n = 2
# xpub1 ="Vpub5gLTnhnQig7SLNhWCqE2AHqt8zhJGQwuwEAKQE67bndddSzUMAmab7DxZF9b9wynVyY2URM61SWY67QYaPV6oQrB41vMKQbeHveRvuThAmm"
# #xpub1 = 'Vpub5dEvVGKn7251znFdWdjnyKjLN525eAmRpVmmZ7chqn8y4Rz9fhKxeeUqJ18mb6kHazADprqMnNyPwG6Y3nGQ2AuchC6VD9ATvn7TLrEXqJz'
# xpub2 ="Vpub5gyCX33B53xAyfEaH1Jfnp5grizbHfxVz6bWLPD92nLcbKMsQzSbM2eyGiK4qiRziuoRhoeVMoPLvEdfbQxGp88PN9cU6zupSSuiPi3RjEg"
# #testcommond.delete_wallet(name)
# testcommond.set_multi_wallet_info(name,m,n)
# testcommond.add_xpub(xpub1)
# testcommond.add_xpub(xpub2)
# testcommond.create_multi_wallet(name)

ret = testcommond.is_valiad_xpub("Vpub5gLTnhnQig7SLNhWCqE2AHqt8zhJGQwuwEAKQE67bndddSzUMAmab7DxZF9b9wynVyY2URM61SWY67QYaPV6oQrB41vMKQbeHveRvuThAmm")
print("=======ret1 = %s" %ret)
ret = testcommond.is_valiad_xpub("Vpub5gLTnhnQig7SLNhWCqE2AHqt8zhJGQwuwEAKQE67bndddSzUMAmab7DxZF9b9wynVyY2URM61SWY67QYaPV6oQrB41v1111111111111111")
print("=======ret2 = %s" %ret)
#testcommond.get_feature()
#testcommond.get_xpub_from_hw()

#path = '/storage/emulated/0/Pictures/test'
#testcommond.save_tx_to_file(path, '123')

#load_wallet
testcommond.load_wallet(name, password)
testcommond.select_wallet(name)
info = testcommond.get_wallets_list_info()

#create_tx
all_output = []
output_info = ['tb1qnuh3qc9g6lwlqqvmf7hg05pzlujhua9emdqdty4znjstr5886paq6htvpe', '0.05']
all_output.append(output_info)
output_str = json.dumps(all_output)
fee = 0.001
message = 'test'
ret_str = testcommond.mktx(output_str, message, fee)
ret_list = json.loads(ret_str)
print("tx================%s" % ret_list['tx'])

#get_tx_by_raw
tx_info_str = testcommond.get_tx_info_from_raw(ret_list['tx'])
tx_info = json.loads(tx_info_str)
print("tx info = %s=========" % tx_info)

#parse_qr tx
qr_data = testcommond.get_qr_data_from_raw_tx(ret_list['tx'])
print("qr_data on ui = %s........" % qr_data)
tx_data = testcommond.parse_qr(qr_data)
print("tx_data = %s---------" % json.loads(tx_data))

#parse_qr_addr
data = testcommond.get_wallet_address_show_UI()

qr_data = json.loads(data)
print("qr_addr = %s------------" % qr_data['qr_data'])

add = testcommond.parse_qr(qr_data['qr_data'])
print("addr = %s--------" %add)
#get_history_tx

data = testcommond.get_tx_info('029a5002de1703279f256bb09c09c6d8fdf8f784b762c26fa6d5f7f9b5de7d6a')
print("get_tx_info = %s-===========" % data)
##get_all_tx_list
#testinfo = testcommond.get_all_tx_list('', None, None)
#print("testinfo = %s------------" %testinfo)
testinfo = testcommond.get_all_tx_list(ret_list['tx'], None, None)
print("testinfo = %s------------" %testinfo)
# testinfo = testcommond.get_all_tx_list(ret_list['tx'], None, 'tobeconfirm')
# print("testinfo = %s------------" %testinfo)
#testinfo = testcommond.get_all_tx_list(ret_list['tx'], None, 'confirmed')
#print("testinfo = %s------------" %testinfo)
# testinfo = testcommond.get_all_tx_list(ret_list['tx'], 'send', None)
# print("testinfo = %s------------" %testinfo)
# testinfo = testcommond.get_all_tx_list(ret_list['tx'], 'receive', None)
# print("testinfo = %s------------" %testinfo)
# testinfo = testcommond.get_all_tx_list(ret_list['tx'], 'send', 'tobeconfirm')
# print("testinfo = %s------------" %testinfo)
# testinfo = testcommond.get_all_tx_list(ret_list['tx'], 'send', 'confirmed')
# print("testinfo = %s------------" %testinfo)
# testinfo = testcommond.get_all_tx_list(ret_list['tx'], 'receive', 'tobeconfirm')
# print("testinfo = %s------------" %testinfo)
# testinfo = testcommond.get_all_tx_list(ret_list['tx'], 'receive', 'confirmed')
# print("testinfo = %s------------" %testinfo)

#get_tx_info

# info = testcommond.get_all_tx_list(ret_list['tx'])
# print("info== %s" % info)
# info = testcommond.get_all_tx_list(ret_list['tx'], tx_status='send', history_status='tobeconfirmed')
# print("info== %s" % info)
# info = testcommond.get_all_tx_list(ret_list['tx'], tx_status='send',history_status='confirmed')
# print("info== %s" % info)
# info = testcommond.get_all_tx_list(ret_list['tx'], tx_status='send', history_status='tobeconfirmed')
# print("info== %s" % info)
# info = testcommond.get_all_tx_list(ret_list['tx'], tx_status='send',history_status='confirmed')
# print("info== %s" % info)
#sign_tx
#testcommond.sign_tx(ret_list['tx'])


