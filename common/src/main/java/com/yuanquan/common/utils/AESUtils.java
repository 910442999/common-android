package com.yuanquan.common.utils;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String ENCODING_FORMAT = "UTF-8"; // 编码格式

    /**
     * AES加密
     *
     * @param key       密钥
     * @param plainText 明文
     * @return 密文
     * @throws Exception 异常
     */
    public static String encrypt(String key, String iv, String plainText) throws Exception {
        byte[] byteKey = key.getBytes(ENCODING_FORMAT);
        SecretKeySpec secretKeySpec = new SecretKeySpec(byteKey, "AES");

        byte[] byteIv = iv.getBytes(ENCODING_FORMAT);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(byteIv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

        byte[] encryptedByte = cipher.doFinal(plainText.getBytes(ENCODING_FORMAT));
        return Base64.encodeToString(encryptedByte, Base64.NO_WRAP);
    }

    /**
     * AES解密
     *
     * @param key          密钥
     * @param encryptedStr 密文
     * @return 明文
     * @throws Exception 异常
     */
    public static String decrypt(String key, String iv, String encryptedStr) throws Exception {
        byte[] byteKey = key.getBytes(ENCODING_FORMAT);
        SecretKeySpec secretKeySpec = new SecretKeySpec(byteKey, "AES");

        byte[] byteIv = iv.getBytes(ENCODING_FORMAT);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(byteIv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

        byte[] encryptedByte = Base64.decode(encryptedStr, Base64.NO_WRAP);
        byte[] decryptedByte = cipher.doFinal(encryptedByte);
        return new String(decryptedByte, ENCODING_FORMAT);
    }
}

