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
#constants.set_regtest()
print("after time = %s" %time.strftime('%Y-%m-%d %H:%M:%S',time.localtime(time.time())))

testcommond = AndroidCommands()
testcommond.start()

#name = 'hahahahhahh777' #2-2 multi wallet
#name = 'hahahahhahh333' #1-N wallet
#name = 'hahahahhahh888' #1-1 wallet
name = 'hahahahhahh999' #software wallet create seed:rocket omit review divert bomb brief mushroom family fatal limb goose lion
password = "111111"
#password = "None"
#test hardware
#testcommond.get_xpub_from_hw()

# #create_wallet 2-N HW wallet
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

#create_wallet 1-N HW wallet

# m = 1
# n = 2
# #xpub1 ="Vpub5gLTnhnQig7SLNhWCqE2AHqt8zhJGQwuwEAKQE67bndddSzUMAmab7DxZF9b9wynVyY2URM61SWY67QYaPV6oQrB41vMKQbeHveRvuThAmm"
# xpub1 = 'Vpub5gDbMdhhmWWW9Y5tr6VU8Mc7JPghZhzv4d73ruD6eiSogEf8kuJywXiyHf3xGEt4jRAUdwTbtjn7LaDUiJpDsHzwT9Gs4KbD1bZNJP4NmeB'
# xpub2 = 'Vpub5g2mF4j2rRtTwdiQjBrqdLiyRKSeRwbEgThABnbCd8kJtPCrfQkdDuJFAfxJrHGH7Hz5fjEx1nwzMoci11hmFaB1Qed9oTfu9Z6BvonP9Qa'
# #xpub2 ="Vpub5gyCX33B53xAyfEaH1Jfnp5grizbHfxVz6bWLPD92nLcbKMsQzSbM2eyGiK4qiRziuoRhoeVMoPLvEdfbQxGp88PN9cU6zupSSuiPi3RjEg"
# testcommond.delete_wallet(name)
# testcommond.set_multi_wallet_info(name,m,n)
# testcommond.add_xpub(xpub1)
# testcommond.add_xpub(xpub2)
# testcommond.create_multi_wallet(name)

# #create_wallet 1-1 HW wallet
#
# m = 1
# n = 1
# #xpub1 ="Vpub5gLTnhnQig7SLNhWCqE2AHqt8zhJGQwuwEAKQE67bndddSzUMAmab7DxZF9b9wynVyY2URM61SWY67QYaPV6oQrB41vMKQbeHveRvuThAmm"
# #xpub1 = 'Vpub5gDbMdhhmWWW9Y5tr6VU8Mc7JPghZhzv4d73ruD6eiSogEf8kuJywXiyHf3xGEt4jRAUdwTbtjn7LaDUiJpDsHzwT9Gs4KbD1bZNJP4NmeB' #VPUB
# xpub1 = 'vpub5VKWEPyGCYx8ixvWuS2VJHGJabeSMMKKkMTNwdwZGwcQ446DzVvhrQs3Ux6UhofAVx6VmMTV1XPcDQbiR5fGiotGcgATev8D7sHViURRbJi'
# #xpub2 = 'Vpub5g2mF4j2rRtTwdiQjBrqdLiyRKSeRwbEgThABnbCd8kJtPCrfQkdDuJFAfxJrHGH7Hz5fjEx1nwzMoci11hmFaB1Qed9oTfu9Z6BvonP9Qa'
# #xpub2 ="Vpub5gyCX33B53xAyfEaH1Jfnp5grizbHfxVz6bWLPD92nLcbKMsQzSbM2eyGiK4qiRziuoRhoeVMoPLvEdfbQxGp88PN9cU6zupSSuiPi3RjEg"
# testcommond.delete_wallet(name)
# testcommond.set_multi_wallet_info(name,m,n)
# testcommond.add_xpub(xpub1)
# #testcommond.add_xpub(xpub2)
# testcommond.create_multi_wallet(name)

## create software wallet by create seed

#name = "test1wwtest"
#password = "111"
#testcommond.delete_wallet(name)
# #seed = testcommond.create(name, password)
#testcommond.create(name, password, seed='rocket omit review divert bomb brief mushroom family fatal limb goose lion')

#ret = testcommond.is_valiad_xpub("Vpub5gLTnhnQig7SLNhWCqE2AHqt8zhJGQwuwEAKQE67bndddSzUMAmab7DxZF9b9wynVyY2URM61SWY67QYaPV6oQrB41vMKQbeHveRvuThAmm")
#print("=======ret1 = %s" %ret)
# ret = testcommond.is_valiad_xpub("Vpub5gLTnhnQig7SLNhWCqE2AHqt8zhJGQwuwEAKQE67bndddSzUMAmab7DxZF9b9wynVyY2URM61SWY67QYaPV6oQrB41v1111111111111111")
# print("=======ret2 = %s" %ret)
#testcommond.get_feature()
#testcommond.get_xpub_from_hw()

#path = '/storage/emulated/0/Pictures/test'
#testcommond.save_tx_to_file(path, '123')

list = testcommond.list_wallets()
print("3333333333-list = %s" %list)


testcommond.set_currency("CNY")
#testcommond.set_currency("None")
#load_wallet
testcommond.get_all_wallet_type_info()
data = testcommond.get_wallet_type(name)
print("11111111111 %s type is %s" %(name, data))
testcommond.load_wallet(name, password)
data = testcommond.select_wallet(name)
print("select data ============%s" %data)

testcommond.set_use_change(False)
testcommond.set_syn_server(False)
#testcommond.delete_wallet(name)
# testcommond.load_wallet(name)
#

# time.sleep(100)
data = testcommond.get_default_server()
print("before data ====%s" %data)

data = testcommond.get_server_list()
print("server_list data =====%s" %data)
#data = testcommond.set_server("testnet.qtornado.com", "51002")
# data = testcommond.set_server("39.97.224.50", "51002")
# data = testcommond.get_default_server()
# print("after data ====%s" %data)

data = testcommond.get_currencies()
print("currencies = %s" %data)
#testcommond.check_seed(seed, password)
#try:
# info = testcommond.get_wallets_list_info()
# print("-----wallets infos = %s" %info)
#except BaseException as e:
#    pass

testcommond.set_base_uint("mBTC")
status = testcommond.get_default_fee_status()
print("status = %s" %status)

exchange = testcommond.get_exchanges()
print("exchange = %s" % exchange)

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

testcommond.set_use_change(False)
testcommond.set_unconf(True)

#create_tx
time.sleep(5)
flag = False
if not flag:
    sign_list = []
    for i in [1, 2]:
        print("i=%s------------" %i)
        all_output = []
        output_info = {'tb1qdvzlw6z7lwr5cgxtglculx3p52su6jw7e9spv2': '0.05'}
        # output_info1 = {'tb1qnuh3qc9g6lwlqqvmf7hg05pzlujhua9emdqdty4znjstr5886paq6htvpe':'0.05'}
        # output_info = {'bcrt1qnuh3qc9g6lwlqqvmf7hg05pzlujhua9emdqdty4znjstr5886paqhwp25r':'0.005'}
        # output_info = {'bcrt1qdvzlw6z7lwr5cgxtglculx3p52su6jw7mvfvmr':'5000'}
        all_output.append(output_info)
        # all_output.append(output_info1)
        output_str = json.dumps(all_output)
        message = 'test111'
        print("--------------all_output= %s" % output_str)
        feerate = testcommond.get_default_fee_status()
        ret_str = testcommond.get_fee_by_feerate(output_str, message, 10)
        ret_list = json.loads(ret_str)
        print("get_fee_by_feerate================%s" % ret_list)

        ret_str = testcommond.mktx(output_str, message)
        ret_list = json.loads(ret_str)
        print("----mktx================%s" % ret_list)

        testinfo = testcommond.get_all_tx_list(None)
        print("----testinfo create = %s------------" % testinfo)
        data = json.loads(testinfo)

        sign_tx = testcommond.sign_tx(ret_list['tx'], password)
        print("==========sign_tx = %s" % sign_tx)
        testinfo = testcommond.get_all_tx_list(None)
        print("testinfo  sign= %s------------" % testinfo)
        data = json.loads(testinfo)
        sign_list.append(sign_tx)
    data = testcommond.get_default_server()
    print("broadcast data ====%s" % data)
    time.sleep(5)
    for sig in sign_list:
        testcommond.broadcast_tx(sig)
elif flag:
    all_output = []
    output_info = {'tb1qdvzlw6z7lwr5cgxtglculx3p52su6jw7e9spv2':'0.05'}
    #output_info1 = {'tb1qnuh3qc9g6lwlqqvmf7hg05pzlujhua9emdqdty4znjstr5886paq6htvpe':'0.05'}
    #output_info = {'bcrt1qnuh3qc9g6lwlqqvmf7hg05pzlujhua9emdqdty4znjstr5886paqhwp25r':'0.005'}
    #output_info = {'bcrt1qdvzlw6z7lwr5cgxtglculx3p52su6jw7mvfvmr':'5000'}
    all_output.append(output_info)
    #all_output.append(output_info1)
    output_str = json.dumps(all_output)
    message = 'test111'
    print("--------------all_output= %s" %output_str)
    feerate = testcommond.get_default_fee_status()
    ret_str = testcommond.get_fee_by_feerate(output_str, message, 10)
    ret_list = json.loads(ret_str)
    print("get_fee_by_feerate================%s" % ret_list)

    ret_str = testcommond.mktx(output_str, message)
    ret_list = json.loads(ret_str)
    print("----mktx================%s" % ret_list)


    # testinfo = testcommond.get_all_tx_list_old()
    # print("hHHHHHHahahaha----testinfo create = %s------------" %testinfo)

    testinfo = testcommond.get_all_tx_list(None)
    print("----testinfo create = %s------------" %testinfo)
    data = json.loads(testinfo)

    # ivoices = testcommond.get_invoices()
    # print("before invoices = %s" %ivoices)

    #test rbf

    # data_hash = data[0]['tx_hash']
    # data = testcommond.get_tx_info(data_hash)
    # print("----hash info create = %s-===========" % data)
    # flag = testcommond.get_rbf_status(data_hash)
    # print("--------rbf data = %s" %flag)
    # data = testcommond.get_rbf_fee_info(data_hash)
    # ret = json.loads(data)
    # print("--------get_rbf_fee_info = %s" %ret)
    # data = testcommond.create_bump_fee(data_hash, ret['new_feerate'], False)
    # new_tx = json.loads(data)
    # print("---------new rbf info = %s" %data)

    # testinfo = testcommond.get_all_tx_list(None)
    # print("----testinfo rbf add= %s------------" %testinfo)
    # data = json.loads(testinfo)
    # data_hash = data[0]['tx_hash']
    # data = testcommond.get_tx_info(data_hash)
    # print("----hash info rbf add = %s-===========" % data)
    # ivoices = testcommond.get_invoices()
    # print("after invoices = %s" %ivoices)
    # testcommond.clear_invoices()
    # ivoices = testcommond.get_invoices()
    # print("clear after invoices = %s" %ivoices)

    #
    # #get_tx_by_raw
    # print("``````````tx %s=========" % new_tx['new_tx'])
    # tx_info_str = testcommond.get_tx_info_from_raw(new_tx['new_tx'])
    # tx_info = json.loads(tx_info_str)
    # print("------tx info = %s=========" % tx_info)
    #sign_tx
    # print("sign tx = %s=========" % new_tx['new_tx'])
    # sign_tx = testcommond.sign_tx(new_tx['new_tx'], password)
    sign_tx = testcommond.sign_tx(ret_list['tx'], password)
    print("==========sign_tx = %s" %sign_tx)
    testinfo = testcommond.get_all_tx_list(None)
    print("testinfo  sign= %s------------" %testinfo)
    data = json.loads(testinfo)

    # for tx_info in data:
    #     data_hash = tx_info['tx_hash']
    #     #print("i = %s type(%s)" %(i,type(i)))
    #     #data_hash = data[i]['tx_hash']
    #     data = testcommond.get_tx_info(data_hash)
    #     print("----hash info sign[%s] = %s-===========" % (data_hash, data))

    # data_hash = '176d7aad5fbe1d9d616d91db391b19aeaf970d22481eae2327039cbf4e295b6e'
    # data = testcommond.get_tx_info(data_hash)
    data = testcommond.get_default_server()
    print("broadcast data ====%s" %data)
    time.sleep(5)
    testcommond.broadcast_tx(sign_tx)

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
time.sleep(10.0)
testinfo = testcommond.get_all_tx_list(None)
print("testinfo  broastcast= %s------------" %testinfo)
data = json.loads(testinfo)
data_hash = data[0]['tx_hash']
data = testcommond.get_tx_info(data_hash)
print("----hash info broastcast = %s-===========" % data)

testinfo = testcommond.get_all_tx_list('send')
print("send.....testinfo = %s------------" %testinfo)
data = json.loads(testinfo)
data_hash = data[0]['tx_hash']
data = testcommond.get_tx_info(data_hash)
print("----send hash info sign = %s-===========" % data)

testinfo = testcommond.get_all_tx_list('receive')
print("receive.....testinfo = %s------------" %testinfo)
data = json.loads(testinfo)
data_hash = data[0]['tx_hash']
data = testcommond.get_tx_info(data_hash)
print("----receive hash info sign = %s-===========" % data)

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

# data = testcommond.get_tx_info_from_raw(ret_list['tx'])
# print("data === %s " %data)
#data = testcommond.get_tx_info('029a5002de1703279f256bb09c09c6d8fdf8f784b762c26fa6d5f7f9b5de7d6a')
#print("get_tx_info = %s-===========" % data)
# data = testcommond.get_default_server()
# print("11111111after data ====%s" %data)