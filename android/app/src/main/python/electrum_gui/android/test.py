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

import time
util.setup_thread_excepthook()
print("before time = %s" %time.strftime('%Y-%m-%d %H:%M:%S',time.localtime(time.time())))
constants.set_testnet()
print("after time = %s" %time.strftime('%Y-%m-%d %H:%M:%S',time.localtime(time.time())))

testcommond = AndroidCommands()
testcommond.start()

name = 'hahahahhahh777'
password = '111111'

#test hardware
#testcommond.get_xpub_from_hw()

# #create_wallet
#
# m = 2
# n = 2
# xpub1 ="Vpub5gLTnhnQig7SLNhWCqE2AHqt8zhJGQwuwEAKQE67bndddSzUMAmab7DxZF9b9wynVyY2URM61SWY67QYaPV6oQrB41vMKQbeHveRvuThAmm"
# #xpub1 = 'Vpub5gDbMdhhmWWW9Y5tr6VU8Mc7JPghZhzv4d73ruD6eiSogEf8kuJywXiyHf3xGEt4jRAUdwTbtjn7LaDUiJpDsHzwT9Gs4KbD1bZNJP4NmeB'
# #xpub2 = 'Vpub5g2mF4j2rRtTwdiQjBrqdLiyRKSeRwbEgThABnbCd8kJtPCrfQkdDuJFAfxJrHGH7Hz5fjEx1nwzMoci11hmFaB1Qed9oTfu9Z6BvonP9Qa'
# xpub2 ="Vpub5gyCX33B53xAyfEaH1Jfnp5grizbHfxVz6bWLPD92nLcbKMsQzSbM2eyGiK4qiRziuoRhoeVMoPLvEdfbQxGp88PN9cU6zupSSuiPi3RjEg"
# testcommond.delete_wallet(name)
# testcommond.set_multi_wallet_info(name,m,n)
# testcommond.add_xpub(xpub1)
# testcommond.add_xpub(xpub2)
# testcommond.create_multi_wallet(name)

# ret = testcommond.is_valiad_xpub("Vpub5gLTnhnQig7SLNhWCqE2AHqt8zhJGQwuwEAKQE67bndddSzUMAmab7DxZF9b9wynVyY2URM61SWY67QYaPV6oQrB41vMKQbeHveRvuThAmm")
# print("=======ret1 = %s" %ret)
# ret = testcommond.is_valiad_xpub("Vpub5gLTnhnQig7SLNhWCqE2AHqt8zhJGQwuwEAKQE67bndddSzUMAmab7DxZF9b9wynVyY2URM61SWY67QYaPV6oQrB41v1111111111111111")
# print("=======ret2 = %s" %ret)
#testcommond.get_feature()
#testcommond.get_xpub_from_hw()

#path = '/storage/emulated/0/Pictures/test'
#testcommond.save_tx_to_file(path, '123')

#testcommond.set_currency("CNY")
testcommond.set_currency("None")
#load_wallet
testcommond.load_wallet(name, password)
data = testcommond.select_wallet(name)
print("select data ============%s" %data)
info = testcommond.get_wallets_list_info()

testcommond.set_base_uint("mBTC")
status = testcommond.get_default_fee_status()
print("status = %s" %status)

exchange = testcommond.get_exchanges()
print("exchange = %s" % json.loads(exchange))

data = testcommond.get_exchange_currency("base", 5)
print("get exchange amount = %s" % data)

data = testcommond.get_exchange_currency("fiat", 302.56)
print("after get exchange amount = %s" % data)
#testcommond.broadcast_tx("02000000000103f9f512c210a473a6f8ae3d9cd1d70a6ca9017456d96a9be5bbb33f4b91bb31340100000000fdffffffad421a234543b89ac2a87408e441037a83fb1eb4a4824e0517d0fd88d293437b0000000000fdffffff7cd7e75c4ca82a2738f4ef8f1d47b1e70f2b03eb178cf2ec313418f02d4c51f20000000000fdffffff02809698000000000022002068a7f776a614653c7ac21226b44014abb28fc6f70105666c7661c6719a2579df34144e050000000022002042cf432cef83c2eaade6063f06a2cf6dde5852219db394e17d70b146eab8c3550400483045022100b04ceb1427db17589427489bdf2b9bdcf6c9c8bd4f70692fe2110e9e5b0d82bc02203359df024dd2cdf146b45c5480b06cfe02445ebfbf430d82ae86695aad97b69501483045022100daee1421ffa2c9014e7cc90cbaba7944c1ec932d608df8b349a1c4546021e57c022000ede4580495457556b8d85483aaafffb3a9f14101c278d4959bb166e2cdcc9d01475221029ee33727df7fb097780b73080c9714b576a548beb3918e0dc686667c1bd8b8b521037df57f86e928ca11feda7d5ad71f64cf5a20675cce72b0b26dbb6701c29dc14652ae04004830450221009a047cc4cd813fde862f7ea21aec12227944b00f8b2e535bd7eeaaa633a9c04e0220186667a639731c60ed4ffbde8e97ecbb7c4d29c76cb4d7ee21755924cddb3ede01483045022100cd8dac67a7e3ac8ed44436c7982ebe95022791cd884ba27ffac47296af599d230220077d86b5e50c0390c4846c16521aec427c4f78f471e5c20a5a1a27ebe6ec001f01475221029ee33727df7fb097780b73080c9714b576a548beb3918e0dc686667c1bd8b8b521037df57f86e928ca11feda7d5ad71f64cf5a20675cce72b0b26dbb6701c29dc14652ae0400483045022100feaf26314f422bb470fe17cd40836e914ea4ba622b6623807992f43b444cda6602200738996c3729288071250b5ded009349233e183ff49a72f94bd8bed134a8a59b014830450221009ed0caff042fc4d668b162648546aa0b20235b71fbf4457fe2cf37bff85ef5cd022016dc5dd6ae2516077faf77523ecf5fb85fc904e83026a0f05e626403e3d535df01475221029ee33727df7fb097780b73080c9714b576a548beb3918e0dc686667c1bd8b8b521037df57f86e928ca11feda7d5ad71f64cf5a20675cce72b0b26dbb6701c29dc14652ae53310000")
#sign_tx
# sign_tx = testcommond.sign_tx("70736274ff0100db0200000003f9f512c210a473a6f8ae3d9cd1d70a6ca9017456d96a9be5bbb33f4b91bb31340100000000fdffffffad421a234543b89ac2a87408e441037a83fb1eb4a4824e0517d0fd88d293437b0000000000fdffffff7cd7e75c4ca82a2738f4ef8f1d47b1e70f2b03eb178cf2ec313418f02d4c51f20000000000fdffffff02809698000000000022002068a7f776a614653c7ac21226b44014abb28fc6f70105666c7661c6719a2579df34144e050000000022002042cf432cef83c2eaade6063f06a2cf6dde5852219db394e17d70b146eab8c355533100000001012b00e1f50500000000220020341d2047d40eddcdf15f4508332a99cdfe834bdca7eb858a8dd96613502649862202029ee33727df7fb097780b73080c9714b576a548beb3918e0dc686667c1bd8b8b5483045022100b04ceb1427db17589427489bdf2b9bdcf6c9c8bd4f70692fe2110e9e5b0d82bc02203359df024dd2cdf146b45c5480b06cfe02445ebfbf430d82ae86695aad97b695010105475221029ee33727df7fb097780b73080c9714b576a548beb3918e0dc686667c1bd8b8b521037df57f86e928ca11feda7d5ad71f64cf5a20675cce72b0b26dbb6701c29dc14652ae2206029ee33727df7fb097780b73080c9714b576a548beb3918e0dc686667c1bd8b8b50cf131eba800000000000000002206037df57f86e928ca11feda7d5ad71f64cf5a20675cce72b0b26dbb6701c29dc1460cf8025b6000000000000000000001012bf40b000000000000220020341d2047d40eddcdf15f4508332a99cdfe834bdca7eb858a8dd96613502649862202029ee33727df7fb097780b73080c9714b576a548beb3918e0dc686667c1bd8b8b54830450221009a047cc4cd813fde862f7ea21aec12227944b00f8b2e535bd7eeaaa633a9c04e0220186667a639731c60ed4ffbde8e97ecbb7c4d29c76cb4d7ee21755924cddb3ede010105475221029ee33727df7fb097780b73080c9714b576a548beb3918e0dc686667c1bd8b8b521037df57f86e928ca11feda7d5ad71f64cf5a20675cce72b0b26dbb6701c29dc14652ae2206029ee33727df7fb097780b73080c9714b576a548beb3918e0dc686667c1bd8b8b50cf131eba800000000000000002206037df57f86e928ca11feda7d5ad71f64cf5a20675cce72b0b26dbb6701c29dc1460cf8025b6000000000000000000001012b0000000000000000220020341d2047d40eddcdf15f4508332a99cdfe834bdca7eb858a8dd96613502649862202029ee33727df7fb097780b73080c9714b576a548beb3918e0dc686667c1bd8b8b5483045022100feaf26314f422bb470fe17cd40836e914ea4ba622b6623807992f43b444cda6602200738996c3729288071250b5ded009349233e183ff49a72f94bd8bed134a8a59b010105475221029ee33727df7fb097780b73080c9714b576a548beb3918e0dc686667c1bd8b8b521037df57f86e928ca11feda7d5ad71f64cf5a20675cce72b0b26dbb6701c29dc14652ae2206029ee33727df7fb097780b73080c9714b576a548beb3918e0dc686667c1bd8b8b50cf131eba800000000000000002206037df57f86e928ca11feda7d5ad71f64cf5a20675cce72b0b26dbb6701c29dc1460cf8025b6000000000000000000000010147522102236c138f904245163fecae96b5b7f72bedebbd0be24cdf34979ea8339b281d452102edb35b9566f0a9b51c24a60f602a1cc8e780e10280fc2976e312b5c2471005a752ae220202236c138f904245163fecae96b5b7f72bedebbd0be24cdf34979ea8339b281d450cf131eba80100000000000000220202edb35b9566f0a9b51c24a60f602a1cc8e780e10280fc2976e312b5c2471005a70cf8025b60010000000000000000")
# print("sign_tx = %s" %sign_tx)
# testcommond.broadcast_tx(sign_tx)
#testcommond.clear_invoices()
#create_tx
all_output = []
output_info = {'tb1qnuh3qc9g6lwlqqvmf7hg05pzlujhua9emdqdty4znjstr5886paq6htvpe':'0.01'}
#output_info1 = {'tb1qnuh3qc9g6lwlqqvmf7hg05pzlujhua9emdqdty4znjstr5886paq6htvpe':'0.05'}
#output_info = {'bcrt1qnuh3qc9g6lwlqqvmf7hg05pzlujhua9emdqdty4znjstr5886paqhwp25r':'0.05'}
#output_info = {'bcrt1q9a4kk79hacd2s838xhdvxmhxrs6tskfp744t7v9pj7f9flayjy0s4d3ttm':'5'}
all_output.append(output_info)
#all_output.append(output_info1)
output_str = json.dumps(all_output)
fee = 150
message = 'test111'
print("--------------all_output= %s" %output_str)
ret_str = testcommond.get_fee_by_feerate(output_str, message, 0.9)
ret_list = json.loads(ret_str)
print("get_fee_by_feerate================%s" % ret_list)

ret_str = testcommond.mktx(output_str, message)
ret_list = json.loads(ret_str)
print("mktx================%s" % ret_list)

ivoices = testcommond.get_invoices()
print("invoices = %s" %ivoices)
# testcommond.clear_invoices()
# ivoices = testcommond.get_invoices()
# print("clear after invoices = %s" %ivoices)

#
# #get_tx_by_raw

tx_info_str = testcommond.get_tx_info_from_raw(ret_list['tx'])
tx_info = json.loads(tx_info_str)
print("tx info = %s=========" % tx_info)
# # #sign_tx
# # #testcommond.sign_tx(ret_list['tx'])
# # #
# # # #parse_qr tx
# qr_data = testcommond.get_qr_data_frparse_prom_raw_tx(ret_list['tx'])
# print("qr_data on ui = %s........" % qr_data)
# tx_data = testcommond.parse_tx(qr_data)
# print("tx_data = %s---------" % json.loads(tx_data))

# #parse_qr_addr
data = testcommond.get_wallet_address_show_UI()
#
# qr_data = json.loads(data)
# print("qr_addr = %s------------" % qr_data['qr_data'])
#
add = testcommond.parse_pr("bitcoin:tb1qnuh3qc9g6lwlqqvmf7hg05pzlujhua9emdqdty4znjstr5886paq6htvpe?amount=5&message=test")
print("addr = %s--------" %add)
# #get_history_tx
#
# ##get_all_tx_list
#testinfo = testcommond.get_all_tx_list('', None, None)
#print("testinfo = %s------------" %testinfo)
testinfo = testcommond.get_all_tx_list(None, None)
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

data = testcommond.get_tx_info_from_raw("020000000001015282796c926b7dc670940bac7abbc7febce874a76653940b88111c594364ddbf01000000171600147d264c997695b8b998f9efcff74fce91bbb6075efeffffff026890210000000000160014d670320606df1e984341206706cce7cf7a5d97b140420f00000000002200209f2f1060a8d7ddf0019b4fae87d022ff257e74b9db40d592a29ca0b1d0e7d07a0247304402200ea749b63fce6983ceb6581e7809be68869176edcf7a4510ab2f8b6a04cf987802207b81cfe080e91b51afc5af7804fd1a68dc791af47da686e2775d2ed228a99259012103488cd2293cedf0d9df588c98e775b09dcea969a0d765ebdfdf5f28ce97b8fa49f1911800")
print("data === %s " %data)
#data = testcommond.get_tx_info('029a5002de1703279f256bb09c09c6d8fdf8f784b762c26fa6d5f7f9b5de7d6a')
#print("get_tx_info = %s-===========" % data)
