/*
*******************************************************************************    
*   Java Card Bitcoin Hardware Wallet
*   (c) 2015 Ledger
*   
*   This program is free software: you can redistribute it and/or modify
*   it under the terms of the GNU Affero General Public License as
*   published by the Free Software Foundation, either version 3 of the
*   License, or (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU Affero General Public License for more details.
*
*   You should have received a copy of the GNU Affero General Public License
*   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*******************************************************************************   
*/    
package com.ledger.wallet;

import javacard.framework.JCSystem;
import javacard.framework.Util;
import javacard.security.CryptoException;
import javacard.security.ECPrivateKey;
import javacard.security.KeyBuilder;
import javacard.security.RandomData;
import javacard.security.Signature;
import javacardx.framework.math.BigNumber;
/**
 * Hardware Wallet crypto tools
 * @author BTChip
 *
 */
public class Crypto {
	
	private static final byte[] MAX_AVAILABLE_SIGNATURE_S = {
            (byte)0x7F,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,
            (byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,
            (byte)0x5D,(byte)0x57,(byte)0x6E,(byte)0x73,(byte)0x57,(byte)0xA4,(byte)0x50,(byte)0x1D,
            (byte)0xDF,(byte)0xE9,(byte)0x2F,(byte)0x46,(byte)0x68,(byte)0X1B,(byte)0x20,(byte)0xA0
    };

    public static void init() {
        sInSignature = JCSystem.makeTransientByteArray((short)256, JCSystem.CLEAR_ON_DESELECT);
        random = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        try {
            // ok, let's save RAM
            transientPrivate = (ECPrivateKey)KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PRIVATE_TRANSIENT_DESELECT, KeyBuilder.LENGTH_EC_FP_256, false);
            transientPrivateTransient = true;
        }
        catch(CryptoException e) {
            try {
                // ok, let's save a bit less RAM
                transientPrivate = (ECPrivateKey)KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PRIVATE_TRANSIENT_RESET, KeyBuilder.LENGTH_EC_FP_256, false);
                transientPrivateTransient = true;
            }
            catch(CryptoException e1) {
                // ok, let's test the flash wear leveling \o/
                transientPrivate = (ECPrivateKey)KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PRIVATE, KeyBuilder.LENGTH_EC_FP_256, false);
                Secp256k1.setCommonCurveParameters(transientPrivate);
            }
        }
        signature = Signature.getInstance(Signature.ALG_ECDSA_SHA_256, false);
    }
    
    public static void initTransientPrivate(byte[] keyBuffer, short keyOffset) {
        if (transientPrivateTransient) {
        	Secp256k1.setCommonCurveParameters(transientPrivate);
        }
        transientPrivate.setS(keyBuffer, keyOffset, (short)32);    	
    }
    public static void preComputedHashSign(byte[] keyBuffer, short keyOffset, byte[] dataBuffer, short dataOffset, byte[] targetBuffer, short targetOffset) {
        initTransientPrivate(keyBuffer, keyOffset);
        Util.arrayFillNonAtomic(keyBuffer, keyOffset, (short)32, (byte)0x00);
        // recheck with the target platform, initializing once instead might be possible and save a few flash write
        // (this part is unspecified in the Java Card API)
        signature.init(transientPrivate, Signature.MODE_SIGN);
        signature.signPreComputedHash(dataBuffer, dataOffset, (short)32, targetBuffer, targetOffset);
        checkSignatureS(targetBuffer);
        if (transientPrivateTransient) {
            transientPrivate.clearKey();
        }
    }
    
    private static void checkSignatureS(byte[] targetBuffer) {
        short sLenOffset = (short)(targetBuffer[3]&0x00ff + 4);
        short sLen = (short)(targetBuffer[sLenOffset] & 0x00ff);
        sLenOffset++;
        Util.arrayCopy(targetBuffer, sLenOffset, sInSignature, (short)0, sLen);
        // when S in signature > 0x7FFFFFFF FFFFFFFF FFFFFFFF FFFFFFFF 5D576E73 57A4501D DFE92F46 681B20A0
        // use S'(0xFFFFFFFF FFFFFFFF FFFFFFFF FFFFFFFE BAAEDCE6 AF48A03B BFD25E8C D0364141 - S)  replace S
        if (Util.arrayCompare(sInSignature, (short)0, MAX_AVAILABLE_SIGNATURE_S, (short)0, sLen) > 0) {
            BigNumber maxS =  new BigNumber((short)32);
            maxS.init(Secp256k1.SECP256K1_R, (short)0, (short)32, BigNumber.FORMAT_HEX);
            maxS.subtract(sInSignature, (short) 0, sLen, BigNumber.FORMAT_HEX);
            // S' = 0xFFFFFFFF FFFFFFFF FFFFFFFF FFFFFFFE BAAEDCE6 AF48A03B BFD25E8C D0364141 - S
            maxS.toBytes(targetBuffer, sLenOffset, sLen, BigNumber.FORMAT_HEX);
        }
    }

    private static byte[] sInSignature;
    protected static ECPrivateKey transientPrivate;    
    protected static boolean transientPrivateTransient;    
    protected static Signature signature;
    protected static RandomData random;
}
