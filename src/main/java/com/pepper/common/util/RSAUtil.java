package com.pepper.common.util;

import com.pepper.common.crypto.RSAKey;

import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/26
 * @Description:RSA非对称加密
 */
public class RSAUtil {
    private static final String KEY_ALGORITHM = "RSA";

    public static final int KEYSIZE_NORMAL = 1024;
    public static final int KEYSIZE_SAFE = 2048;

    /**
     * 生成密钥
     *
     * @param keySize 建议至少1024，否则不安全
     * @return
     */
    public static RSAKey generateKey(int keySize) {
        keySize = keySize > 0 ? keySize : KEYSIZE_NORMAL;

        KeyPairGenerator keyPairGen = null;
        try {
            keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // 初始化
        keyPairGen.initialize(keySize);

        // 生成密钥对
        KeyPair keyPair = keyPairGen.generateKeyPair();

        RSAKey rsaKey = new RSAKey();

        // 公钥
        rsaKey.setPublicKey(((RSAPublicKey) keyPair.getPublic()).getEncoded());
        // 私钥
        rsaKey.setPrivateKey(((RSAPrivateKey) keyPair.getPrivate()).getEncoded());

        return rsaKey;
    }

    /**
     * 根据私钥加密数据
     *
     * @param data
     * @param privateKey
     * @return
     */
    public static byte[] encryptByPrivateKey(byte[] data, byte[] privateKey) {
        // 获取私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKey);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);

            // 生成私钥
            PrivateKey key = keyFactory.generatePrivate(pkcs8KeySpec);

            // 对数据加密
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, key);

            return cipher.doFinal(data);
        } catch (Exception e) {
            throw  new RuntimeException(e);
        }
    }

    /**
     * 根据公钥加密数据
     *
     * @param data
     * @param publicKey
     * @return
     */
    public static byte[] encryptByPublicKey(byte[] data, byte[] publicKey) {
        // 获取公钥
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PublicKey key = keyFactory.generatePublic(x509KeySpec);

            // 对数据加密
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, key);

            return cipher.doFinal(data);
        } catch (Exception e) {
            throw  new RuntimeException(e);
        }
    }

    /**
     * 根据私钥解密数据
     *
     * @param data
     * @param privateKey
     * @return
     */
    public static byte[] decryptByPrivateKey(byte[] data, byte[] privateKey) {
        // 获取私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKey);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);

            // 生成私钥
            PrivateKey key = keyFactory.generatePrivate(pkcs8KeySpec);

            // 对数据加密
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, key);

            return cipher.doFinal(data);
        } catch (Exception e) {
            throw  new RuntimeException(e);
        }
    }

    /**
     * 根据公钥解密数据
     *
     * @param data
     * @param publicKey
     * @return
     */
    public static byte[] decryptByPublicKey(byte[] data, byte[] publicKey) {
        // 获取公钥
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PublicKey key = keyFactory.generatePublic(x509KeySpec);

            // 对数据加密
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, key);

            return cipher.doFinal(data);
        } catch (Exception e) {
            throw  new RuntimeException(e);
        }
    }

}
