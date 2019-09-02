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

/* This file is automatically processed from the .javap version and only included for convenience. Please refer to the .javap file
   for more readable code */

package com.ledger.wallet;
import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.CardRuntimeException;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.OwnerPIN;
import javacard.framework.Util;
import javacard.security.ECPrivateKey;
import javacard.security.KeyBuilder;

public class LedgerWalletApplet extends Applet {
    public LedgerWalletApplet(byte[] parameters, short parametersOffset, byte parametersLength) {
        Crypto.init();
        WalletCache.init();
        scratch256 = JCSystem.makeTransientByteArray((short)256, JCSystem.CLEAR_ON_DESELECT);
        walletPin = new OwnerPIN(WALLET_PIN_ATTEMPTS, WALLET_PIN_SIZE);
        secondaryPin = new OwnerPIN(SECONDARY_PIN_ATTEMPTS, SECONDARY_PIN_SIZE);
        if (parametersLength != 0) {
            attestationPrivate = (ECPrivateKey)KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PRIVATE, KeyBuilder.LENGTH_EC_FP_256, false);
            attestationPublic = new byte[65];
            Secp256k1.setCommonCurveParameters(attestationPrivate);
            attestationPrivate.setS(parameters, parametersOffset, (short)32);
            parametersOffset += (short)32;
            attestationSignature = new byte[parameters[(short)(parametersOffset + 1)] + 2];
            Util.arrayCopy(parameters, parametersOffset, attestationSignature, (short)0, (short)attestationSignature.length);
        }
    }

    protected static boolean isContactless() {
        return ((APDU.getProtocol() & APDU.PROTOCOL_MEDIA_MASK) == APDU.PROTOCOL_MEDIA_CONTACTLESS_TYPE_A);
    }
    private static void handleSetup(APDU apdu) throws ISOException {
        byte[] buffer = apdu.getBuffer();
        short offset = ISO7816.OFFSET_CDATA;
        if ((setup == TRUE) || (setup != FALSE)) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
        if (buffer[ISO7816.OFFSET_P1] != P1_REGULAR_SETUP) {
         ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }
        walletPinSize = buffer[offset++];
        if (walletPinSize < 4) {
         ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }
        Util.arrayFillNonAtomic(scratch256, (short)0, WALLET_PIN_SIZE, (byte)0xff);
        Util.arrayCopyNonAtomic(buffer, offset, scratch256, (short)0, walletPinSize);
        walletPin.update(scratch256, (short)0, WALLET_PIN_SIZE);
        walletPin.resetAndUnblock();
        offset += walletPinSize;
        secondaryPinSize = buffer[offset++];
        if (secondaryPinSize != 0) {
            Util.arrayFillNonAtomic(scratch256, (short)0, SECONDARY_PIN_SIZE, (byte)0xff);
            Util.arrayCopyNonAtomic(buffer, offset, scratch256, (short)0, secondaryPinSize);
            secondaryPin.update(scratch256, (short)0, SECONDARY_PIN_SIZE);
            secondaryPin.resetAndUnblock();
            offset += secondaryPinSize;
        }
        apdu.setOutgoingAndSend((short)0, offset);
        setup = TRUE;
    }
    public static void clearScratch() {
            Util.arrayFillNonAtomic(scratch256, (short)0, (short)scratch256.length, (byte)0x00);
    }
    public void process(APDU apdu) throws ISOException {
        if (selectingApplet()) {
            return;
        }
        byte[] buffer = apdu.getBuffer();
        apdu.setIncomingAndReceive();
        if (buffer[ISO7816.OFFSET_CLA] == CLA_BTC) {
            clearScratch();
        if (isContactless()) {
        apdu.waitExtension();
        }
            try {
                switch(buffer[ISO7816.OFFSET_INS]) {
                    case INS_SETUP:
                        handleSetup(apdu);
                        break;
                    case INS_SIGN:
                        handleSign(apdu);
                        break;
                    case INS_EXPORT_PRV:
                        handleExportSecretKey(apdu);
                        break;
                    case INS_EXPORT_PUB:
                        handleExportPublicKey(apdu);
                    case INS_IMPORT:
                        handleImportSecretKey(apdu);
                        break;
                    default:
                        ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
                }
            }
            catch(Exception e) {
                if (e instanceof CardRuntimeException) {
                    throw ((CardRuntimeException)e);
                }
                else {
                    ISOException.throwIt(ISO7816.SW_UNKNOWN);
                }
            }
            finally {
                clearScratch();
            }
            return;
        } else {
         ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }
    }

    private static void handleImportSecretKey(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte offset = ISO7816.OFFSET_LC;
        if (buffer[offset] != (byte)97) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        offset++;
        WalletCache.importKey(buffer, offset);
    }

    private static void handleExportSecretKey(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        if (!WalletCache.exportKey(buffer, (byte) 0, true)) {
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }
        apdu.setOutgoingAndSend((short) 0, (short) 32);
    }
    private static void handleExportPublicKey(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        if (!WalletCache.exportKey(buffer, (byte) 0, false)) {
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }
        apdu.setOutgoingAndSend((short) 0, (short) 65);
    }

    private static void handleSign(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte offset = ISO7816.OFFSET_LC;
        if (buffer[offset] != 97) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        offset++;
        if (!WalletCache.getCorrectKeyToSign(buffer, offset, scratch256, (short) 0)) {
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }
        offset += 65;
        Crypto.preComputedHashSign(scratch256, (short)0, buffer, (short)offset, buffer, (short)0);
        short signatureSize = (short)((short)(buffer[1] & 0xff) + 2);
        buffer[signatureSize] = 0x01;
        apdu.setOutgoingAndSend((short)0, (short)(signatureSize + 1));
    }
    public static void install (byte bArray[], short bOffset, byte bLength) throws ISOException {
        short offset = bOffset;
        offset += (short)(bArray[offset] + 1);
        offset += (short)(bArray[offset] + 1);
        new LedgerWalletApplet(bArray, (short)(offset + 1), bArray[offset]).register(bArray, (short)(bOffset + 1), bArray[bOffset]);
    }
    private static final byte WALLET_PIN_ATTEMPTS = (byte)3;
    private static final byte WALLET_PIN_SIZE = (byte)32;
    private static final byte SECONDARY_PIN_ATTEMPTS = (byte)3;
    private static final byte SECONDARY_PIN_SIZE = (byte)4;
    private static final byte CLA_BTC = (byte)0x00;
    private static final byte INS_SETUP = (byte)0x20;
    private static final byte INS_SIGN = (byte)0x06;
    private static final byte INS_IMPORT = (byte) 0x07;
    private static final byte INS_EXPORT_PRV = (byte) 0x08;
    private static final byte INS_EXPORT_PUB = (byte) 0x05;
    private static final byte P1_REGULAR_SETUP = (byte)0x00;
    public static byte[] scratch256;
    private static OwnerPIN walletPin;
    private static byte walletPinSize;
    private static OwnerPIN secondaryPin;
    private static byte secondaryPinSize;
    private static byte setup;
    protected static ECPrivateKey attestationPrivate;
    protected static byte[] attestationPublic;
    protected static byte[] attestationSignature;
    protected static final byte TRUE = (byte)0x37;
    protected static final byte FALSE = (byte)0xda;
}
