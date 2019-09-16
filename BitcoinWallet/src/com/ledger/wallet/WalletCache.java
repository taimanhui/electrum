package com.ledger.wallet;

import javacard.framework.Util;
import javacard.security.ECKey;
import javacard.security.ECPrivateKey;
import javacard.security.ECPublicKey;
import javacard.security.KeyBuilder;
import javacard.security.KeyPair;

public class WalletCache {
    private byte[] privateKey;
    private byte[] unCompressedPublicKey;
    // 密钥对缓存对象
    private static WalletCache[] cache = null;
    public WalletCache() {
        privateKey = new byte[32];
        unCompressedPublicKey = new byte[65];
    }
    /***
     * 初始化3对密钥对并保存在密钥缓存中
     * */
    public static void init() {
        cache = new WalletCache[1];
        cache[0] = new WalletCache();
        ECKey ecKey = (ECKey)KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PUBLIC, KeyBuilder.LENGTH_EC_FP_256, false);
        // 添加椭圆曲线常量参数限制
        Secp256k1.setCommonCurveParameters(ecKey);
        KeyPair keyPair = new KeyPair(
                (ECPublicKey)(ecKey),
                (ECPrivateKey)KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PRIVATE, KeyBuilder.LENGTH_EC_FP_256, false));
        keyPair.genKeyPair();
        ((ECPublicKey)keyPair.getPublic()).getW(cache[0].unCompressedPublicKey, (short)0);
        ((ECPrivateKey)keyPair.getPrivate()).getS(cache[0].privateKey, (short)0);
    }

    /****
     * 导入密钥对
     * */
    public static void importKey(byte[]src, short srcOffset) {
        cache[0] = new WalletCache();
        Util.arrayCopyNonAtomic(src, srcOffset, cache[0].privateKey, (short)0, (short) 32);
        srcOffset += 32;
        Util.arrayCopyNonAtomic(src, srcOffset, cache[0].unCompressedPublicKey,(short)0, (short)65);
    }

    /***
     *
     * 按索引导出密钥
     * */
    public static boolean exportKey(byte[] des,  byte desOffset, boolean getPrivate) {
        if (getPrivate) {
            Util.arrayCopy(cache[0].privateKey, (short) 0, des, desOffset, (short) 32);
        } else {
            Util.arrayCopy(cache[0].unCompressedPublicKey, (short) 0, des, desOffset, (short) 65);
        }
        return true;
    }

    public static boolean getCorrectKeyToSign(byte[] inputData, short inOffset, byte[] target, short targetOffset) {
          if (Util.arrayCompare(cache[0].unCompressedPublicKey, (short)0, inputData, inOffset, (short) 65) == 0) {
              Util.arrayCopy(cache[0].privateKey, (short)0, target, targetOffset, (short)32);
              return true;
           }
        return false;
    }

}
