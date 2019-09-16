'''This is the Android implementation of NFC Scanning using the
built in NFC adapter of some android phones.
'''

from kivy.app import App
from kivy.clock import Clock
#Detect which platform we are on
from kivy.utils import platform
if platform != 'android':
    raise ImportError
import threading
import binascii

from . import NFCBase
from jnius import autoclass, cast
from android.runnable import run_on_ui_thread
from android import activity

from electrum.gui.kivy.btchipHelpers import *
from electrum.gui.kivy.bitcoinVarint import *

BUILDVERSION = autoclass('android.os.Build$VERSION').SDK_INT
NfcAdapter = autoclass('android.nfc.NfcAdapter')
PythonActivity = autoclass('org.kivy.android.PythonActivity')
JString = autoclass('java.lang.String')
Charset = autoclass('java.nio.charset.Charset')
locale = autoclass('java.util.Locale')
Intent = autoclass('android.content.Intent')
IntentFilter = autoclass('android.content.IntentFilter')
PendingIntent = autoclass('android.app.PendingIntent')
Ndef = autoclass('android.nfc.tech.Ndef')
NdefRecord = autoclass('android.nfc.NdefRecord')
NdefMessage = autoclass('android.nfc.NdefMessage')
IsoDep = autoclass('android.nfc.tech.IsoDep')

app = None


BTCHIP_CLA = 0x00
BTCHIP_JC_EXT_CLA = 0x20
BTCHIP_INS_SETUP = 0x20
BTCHIP_INS_HASH_SIGN = 0x12
BTCHIP_INS_EXT_CACHE_HAS_PUBLIC_KEY = 0x21
BTCHIP_INS_EXT_GET_HALF_PUBLIC_KEY = 0x20
BTCHIP_INS_EXT_CACHE_PUT_PUBLIC_KEY = 0x22
BTCHIP_INS_GET_WALLET_PUBLIC_KEY = 0x40

OPERATION_MODE_WALLET = 0x01
FEATURE_RFC6979 = 0x02

SELECT_APPLET_CMD = "00A404000611223344550100"

class ScannerAndroid(NFCBase):
    ''' This is the class responsible for handling the interface with the
    Android NFC adapter. See Module Documentation for details.
    '''
    name = 'NFCAndroid'
    needKeyCache = True
    def nfc_init(self, callback):
        ''' This is where we initialize NFC adapter.
        '''
        # Initialize NFC
        global app
        app = App.get_running_app()
        self.callback = callback
        # Make sure we are listening to new intent 
        activity.bind(on_new_intent=self.on_new_intent)

        # Configure nfc
        self.j_context = context = PythonActivity.mActivity
        self.nfc_adapter = NfcAdapter.getDefaultAdapter(context)
        # Check if adapter exists
        if not self.nfc_adapter:
            return False
        # specify that we want our activity to remain on top when a new intent
        # is fired
        self.nfc_pending_intent = PendingIntent.getActivity(context, 0,
            Intent(context, context.getClass()).addFlags(
                Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)

        # Filter for different types of action, by default we enable all.
        # These are only for handling different NFC technologies when app is in foreground
        self.ndef_detected = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        #self.tech_detected = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        #self.tag_detected = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)

        # setup tag discovery for ourt tag type
        try:
            self.ndef_detected.addCategory(Intent.CATEGORY_DEFAULT)
            # setup the foreground dispatch to detect all mime types
            self.ndef_detected.addDataType('*/*')

            self.ndef_exchange_filters = [self.ndef_detected]
        except Exception as err:
            raise Exception(repr(err))
        return True

    def get_ndef_details(self, tag):
        ''' Get all the details from the tag.
        '''
        details = {}

        try:
            #print 'id'
            details['uid'] = ':'.join(['{:02x}'.format(bt & 0xff) for bt in tag.getId()])
            #print 'technologies'
            details['Technologies'] = tech_list = [tech.split('.')[-1] for tech in tag.getTechList()]
            #print 'get NDEF tag details'
            ndefTag = cast('android.nfc.tech.Ndef', Ndef.get(tag))
            #print 'tag size'
            details['MaxSize'] = ndefTag.getMaxSize()
            #details['usedSize'] = '0'
            #print 'is tag writable?'
            details['writable'] = ndefTag.isWritable()
            #print 'Data format'
            # Can be made readonly
            # get NDEF message details
            ndefMesg = ndefTag.getCachedNdefMessage()
            # get size of current records
            details['consumed'] = len(ndefMesg.toByteArray())
            #print 'tag type'
            details['Type'] = ndefTag.getType()

            # check if tag is empty
            if not ndefMesg:
                details['Message'] = None
                return details

            ndefrecords =  ndefMesg.getRecords()
            length = len(ndefrecords)
            #print 'length', length
            # will contain the NDEF record types
            recTypes = []
            for record in ndefrecords:
                recTypes.append({
                    'type': ''.join(map(chr, record.getType())),
                    'payload': ''.join(map(chr, record.getPayload()))
                    })

            details['recTypes'] = recTypes
        except Exception as err:
            print(str(err))

        return details

    def on_new_intent(self, intent):
        ''' This function is called when the application receives a
        new intent, for the ones the application has registered previously,
        either in the manifest or in the foreground dispatch setup in the
        nfc_init function above.
        '''
        print("on_new_intent in................")
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        action_list = (NfcAdapter.ACTION_NDEF_DISCOVERED,
                       NfcAdapter.ACTION_TECH_DISCOVERED,
                       NfcAdapter.ACTION_TAG_DISCOVERED)
        if intent.getAction() not in action_list:
            print('unknow action, avoid.')
            return
        tag = cast('android.nfc.Tag', intent.getParcelableExtra(NfcAdapter.EXTRA_TAG))
        self.isoDep = IsoDep.get(tag)
        if self.isoDep:
            self.isoDep.connect()
            self.isoDep.setTimeout(5000)
        selectcmd = binascii.unhexlify(SELECT_APPLET_CMD)
        result = self.isoDep.transceive(selectcmd)
        print('transaction on_new_intent select result.............',
              ''.join(['%02X ' % b for b in result]))
        self.callback()

    def nfc_disable(self):
        '''Disable app from handling tags.
        '''
        self.disable_foreground_dispatch()

    def nfc_enable(self):
        '''Enable app to handle tags when app in foreground.
        '''
        self.enable_foreground_dispatch()

    def create_AAR(self):
        '''Create the record responsible for linking our application to the tag.
        '''
        return NdefRecord.createApplicationRecord(JString("org.electrum.kivy"))

    def create_TNF_EXTERNAL(self, data):
        '''Create our actual payload record.
        '''
        if BUILDVERSION >= 14:
            domain = "org.electrum"
            stype = "externalType"
            extRecord = NdefRecord.createExternal(domain, stype, data)
        else:
            # Creating the NdefRecord manually:
            extRecord = NdefRecord(
                NdefRecord.TNF_EXTERNAL_TYPE,
                "org.electrum:externalType",
                '',
                data)
        return extRecord

    def isoDepClose(self):
        self.isoDep.close()

    def create_ndef_message(self, *recs):
        ''' Create the Ndef message that will be written to tag
        '''
        records = []
        for record in recs:
            if record:
                records.append(record)

        return NdefMessage(records)


    @run_on_ui_thread
    def disable_foreground_dispatch(self):
        '''Disable foreground dispatch when app is paused.
        '''
        self.nfc_adapter.disableForegroundDispatch(self.j_context)

    @run_on_ui_thread
    def enable_foreground_dispatch(self):
        '''Start listening for new tags
        '''
        self.nfc_adapter.enableForegroundDispatch(self.j_context,
                self.nfc_pending_intent, None, None)

    @run_on_ui_thread
    def _nfc_enable_ndef_exchange(self, data):
        # Enable p2p exchange
        # Create record
        ndef_record = NdefRecord(
                NdefRecord.TNF_MIME_MEDIA,
                'org.electrum.kivy', '', data)
        
        # Create message
        ndef_message = NdefMessage([ndef_record])

        # Enable ndef push
        self.nfc_adapter.enableForegroundNdefPush(self.j_context, ndef_message)

        # Enable dispatch
        self.nfc_adapter.enableForegroundDispatch(self.j_context,
                self.nfc_pending_intent, self.ndef_exchange_filters, [])

    @run_on_ui_thread
    def _nfc_disable_ndef_exchange(self):
        # Disable p2p exchange
        self.nfc_adapter.disableForegroundNdefPush(self.j_context)
        self.nfc_adapter.disableForegroundDispatch(self.j_context)

    def nfc_enable_exchange(self, data):
        '''Enable Ndef exchange for p2p
        '''
        self._nfc_enable_ndef_exchange()

    def nfc_disable_exchange(self):
        ''' Disable Ndef exchange for p2p
        '''
        self._nfc_disable_ndef_exchange()

    def parse_bip32_path_internal(self, path):
        if len(path) == 0:
            return []
        result = []
        elements = path.split('/')
        for pathElement in elements:
            element = pathElement.split('\'')
            if len(element) == 1:
                result.append(int(element[0]))
            else:
                result.append(0x80000000 | int(element[0]))
        return result

    def sign_hash(self, path, hashdata):
        donglePath = parse_bip32_path(path)
        if self.needKeyCache:
             self.resolvePublicKeysInPath(path)
        apdu = [BTCHIP_CLA, BTCHIP_INS_HASH_SIGN, 0x00, 0x00 ]
        params = []
        params.extend(donglePath)
        params.extend(bytearray(hashdata))
        apdu.append(len(params))
        apdu.extend(params)
        send = bytearray(apdu)
        
        sig = self.exchange(send)
        return sig

    def exchange(self, data):
        print('exchange send apdu.............',
               ''.join(['%02X ' % b for b in data]))
        result = self.isoDep.transceive(data)
        print('exchange result.............',
               ''.join(['%02X ' % b for b in result]))
        return result
        
    # def setup(self, operationModeFlags, featuresFlag, keyVersion, keyVersionP2SH, userPin, wipePin, keymapEncoding, seed=None, developerKey=None):
    #     if isinstance(userPin, str):
    #         userPin = userPin.encode('utf-8')
    #     result = {}
    #     params = []
    #     apdu = [ BTCHIP_CLA, BTCHIP_INS_SETUP, 0x00, 0x00 ]
    #     if seed is not None:
    #         params.append(len(seed))
    #         params.extend(seed)
    #     apdu.append(len(params))
    #     apdu.extend(params)
    #     response = self.exchange(bytearray(apdu))
    #     result['trustedInputKey'] = response[0:16]
    #     result['developerKey'] = response[16:]
    #     return result

    def getWalletPublicKey(self, path, showOnScreen=False, segwit=False, segwitNative=False, cashAddr=False):
        result = {}
        donglePath = parse_bip32_path(path)
        if self.needKeyCache:
            self.resolvePublicKeysInPath(path)
        apdu = [ BTCHIP_CLA, BTCHIP_INS_GET_WALLET_PUBLIC_KEY, 0x01 if showOnScreen else 0x00, 0x03 if cashAddr else 0x02 if segwitNative else 0x01 if segwit else 0x00, len(donglePath) ]
        apdu.extend(donglePath)
        response = self.exchange(bytearray(apdu))
        offset = 0
        result['publicKey'] = response[offset + 1 : offset + 1 + response[offset]]
        offset = offset + 1 + response[offset]
        #result['address'] = str(response[offset + 1 : offset + 1 + response[offset]])
        #offset = offset + 1 + response[offset]
        result['chainCode'] = response[offset : offset + 32]
        return result

    def resolvePublicKeysInPath(self, path):
        splitPath = self.parse_bip32_path_internal(path)
        # Locate the first public key in path
        offset = 0
        startOffset = 0
        while(offset < len(splitPath)):
            if (splitPath[offset] < 0x80000000):
                startOffset = offset
                break
            offset = offset + 1
        if startOffset != 0:
            searchPath = splitPath[0:startOffset - 1]
            offset = startOffset - 1
            while(offset < len(splitPath)):
                searchPath = searchPath + [ splitPath[offset] ]
                self.resolvePublicKey(searchPath)
                offset = offset + 1
        self.resolvePublicKey(splitPath)

    def parse_bip32_path_internal(self, path):
        if len(path) == 0:
            return []
        result = []
        elements = path.split('/')
        for pathElement in elements:
            element = pathElement.split('\'')
            if len(element) == 1:
                result.append(int(element[0]))
            else:
                result.append(0x80000000 | int(element[0]))
        return result

    def serialize_bip32_path_internal(self, path):
        result = []
        for pathElement in path:
            writeUint32BE(pathElement, result)
        return bytearray([ len(path) ] + result)
        
    def resolvePublicKey(self, path):
        expandedPath = self.serialize_bip32_path_internal(path)
        # apdu = [ BTCHIP_JC_EXT_CLA, BTCHIP_INS_EXT_CACHE_HAS_PUBLIC_KEY, 0x00, 0x00 ]
        # apdu.append(len(expandedPath))
        # apdu.extend(expandedPath)
        # result = self.exchange(bytearray(apdu))
        # if (result[0] == 0):
            # Not present, need to be inserted into the cache
        apdu = [ BTCHIP_JC_EXT_CLA, BTCHIP_INS_EXT_GET_HALF_PUBLIC_KEY, 0x00, 0x00 ]
        apdu.append(len(expandedPath))
        apdu.extend(expandedPath)
        result = self.exchange(bytearray(apdu))
        hashData = result[0:32]
        keyX = result[32:64]
        signature = result[64:-2]
        hashData = ''.join(['%02X' % b for b in hashData])
        print("hashdata = %s" %hashData)
        keyX = ''.join(['%02X' % b for b in keyX])
        print("keyx = %s" %keyX)
        signature = ''.join(['%02X' % b for b in signature])
        print("signature = %s" %signature)
        keyXY = self.recoverKey(signature, hashData, keyX)
        apdu = [ BTCHIP_JC_EXT_CLA, BTCHIP_INS_EXT_CACHE_PUT_PUBLIC_KEY, 0x00, 0x00 ]
        apdu.append(len(expandedPath) + 65)
        apdu.extend(expandedPath)
        apdu.extend(keyXY)
        self.exchange(bytearray(apdu))

    def recoverKey(self, signature, hashValue, keyX):
        signature = binascii.unhexlify(signature)
        hashValue = binascii.unhexlify(hashValue)
        keyX = binascii.unhexlify(keyX)
        rLength = signature[3]
        r = signature[4: 4 + rLength]
        sLength = signature[4 + rLength + 1]
        s = signature[4 + rLength + 2:]
        if rLength == 33:
            r = r[1:]
        if sLength == 33:
            s = s[1:]

        for i in range(4):
            try:
                from electrum.ecc import _MyVerifyingKey, point_to_ser, SECP256k1
                print("len(r+s) = %s" %len(r+s))
                key = _MyVerifyingKey.from_signature(r+s, i, hashValue, curve=SECP256k1)
                candidate = point_to_ser(key.pubkey.point, False)
                if candidate[1:33] == keyX:
                    return candidate
            except:
                pass
        raise Exception("Key recovery failed")

scan = ScannerAndroid()