import decimal
import re

class BTChipException(Exception):

    def __init__(self, message, sw=0x6f00):
        self.message = message
        self.sw = sw

    def __str__(self):
        buf = "Exception : " + self.message
        return buf

# from pycoin
SATOSHI_PER_COIN = decimal.Decimal(1e8)
COIN_PER_SATOSHI = decimal.Decimal(1)/SATOSHI_PER_COIN

def satoshi_to_btc(satoshi_count):
    if satoshi_count == 0:
        return decimal.Decimal(0)
    r = satoshi_count * COIN_PER_SATOSHI
    return r.normalize()

def btc_to_satoshi(btc):
    return int(decimal.Decimal(btc) * SATOSHI_PER_COIN)
# /from pycoin

def writeUint32BE(value, buffer):
    buffer.append((value >> 24) & 0xff)
    buffer.append((value >> 16) & 0xff)
    buffer.append((value >> 8) & 0xff)
    buffer.append(value & 0xff)
    return buffer

def writeUint32LE(value, buffer):
    buffer.append(value & 0xff)
    buffer.append((value >> 8) & 0xff)
    buffer.append((value >> 16) & 0xff)
    buffer.append((value >> 24) & 0xff)
    return buffer

def writeHexAmount(value, buffer):
    buffer.append(value & 0xff)
    buffer.append((value >> 8) & 0xff)
    buffer.append((value >> 16) & 0xff)
    buffer.append((value >> 24) & 0xff)
    buffer.append((value >> 32) & 0xff)
    buffer.append((value >> 40) & 0xff)
    buffer.append((value >> 48) & 0xff)
    buffer.append((value >> 56) & 0xff)
    return buffer

def writeHexAmountBE(value, buffer):
    buffer.append((value >> 56) & 0xff)
    buffer.append((value >> 48) & 0xff)
    buffer.append((value >> 40) & 0xff)
    buffer.append((value >> 32) & 0xff)
    buffer.append((value >> 24) & 0xff)
    buffer.append((value >> 16) & 0xff)
    buffer.append((value >> 8) & 0xff)
    buffer.append(value & 0xff)
    return buffer

def parse_bip32_path(path):
    if len(path) == 0:
        return bytearray([0])
    result = []
    elements = path.split('/')
    if len(elements) > 10:
        print("Path too long.............")
    for pathElement in elements:
        element = re.split('\'|h|H', pathElement)
        if len(element) == 1:
            writeUint32BE(int(element[0]), result)
        else:
            writeUint32BE(0x80000000 | int(element[0]), result)
    return bytearray([len(elements)]+result)

def compress_public_key(publicKey):
    if publicKey[0] == 0x04:
        print("pub[0] = 04........")
        if (publicKey[64] & 1) != 0:
            print("publicKey[64] & 1) != 0 %02X" %(publicKey[64] & 1))
            prefix = 0x03
        else:
            print("publicKey[64] & 1) == 0 %02X" %(publicKey[64] & 1))
            prefix = 0x02
        result = [prefix]
        result.extend(publicKey[1:33])
        return bytearray(result)
    elif publicKey[0] == 0x03 or publicKey[0] == 0x02:
        print("publicKey[0] == 0x03 or publicKey[0] == 0x02")
        return publicKey
    else:
        raise BTChipException("Invalid public key format")