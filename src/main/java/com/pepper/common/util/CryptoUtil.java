package com.pepper.common.util;


import com.pepper.common.crypto.Crypto;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.Key;
import java.security.spec.KeySpec;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/26
 * @Description:
 */
public class CryptoUtil{
        /**
         * 生成DES加密密钥
         *
         * @return
         */
        public static byte[] generateKey(Crypto.Algorithm algorithm) {
            KeyGenerator keyGenerator = null;
            try {
                keyGenerator = KeyGenerator.getInstance(algorithm.getName());
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            keyGenerator.init(algorithm.getKeySize());

            // 生成密钥
            SecretKey secretKey = keyGenerator.generateKey();
            return secretKey.getEncoded();
        }

        /**
         * 转换密钥
         *
         * @param key 密钥
         * @return
         */
        private static Key toKey(Crypto.Algorithm algorithm, byte[] key) {
            try {
                KeySpec keySpec = null;
                if (algorithm.getName().equals("DES")) {
                    keySpec = new DESKeySpec(key);
                } else if (algorithm.getName().equals("DESede")) {
                    keySpec = new DESedeKeySpec(key);
                } else if (algorithm.getName().equals("AES")) {
                    return new SecretKeySpec(key, "AES");
                } else {
                    return new SecretKeySpec(key, algorithm.getName());
                }

                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithm.getName());
                return keyFactory.generateSecret(keySpec);
            } catch (NoSuchAlgorithmException e) {
                return new SecretKeySpec(key, algorithm.getName());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 加密
         *
         * @param data  待加密数据
         * @param mode  工作模式
         * @param padding 填充方式
         * @param key   密钥
         * @return
         */
        public static byte[] encrypt(byte[] data, Crypto.Algorithm algorithm,
                                     Crypto.Mode mode, Crypto.Padding padding, byte[] key) {
            Key k = toKey(algorithm, key);

            // 实例化
            String algorithmKey = algorithm.getName() + "/" + mode.getName() + "/" + padding.getName();

            try {
                Cipher cipher = Cipher.getInstance(algorithmKey);

                // 初始化，设置为加密模式
                cipher.init(Cipher.ENCRYPT_MODE, k);

                // 执行操作
                return cipher.doFinal(data);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 解密
         *
         * @param data  待解密数据
         * @param mode  工作模式
         * @param padding 填充方式
         * @param key   密钥
         * @return
         */
        public static byte[] decrypt(byte[] data, Crypto.Algorithm algorithm,
                                     Crypto.Mode mode, Crypto.Padding padding, byte[] key) {
            Key k = toKey(algorithm, key);

            // 实例化
            String algorithmKey = algorithm.getName() + "/" + mode.getName() + "/" + padding.getName();

            try {
                Cipher cipher = Cipher.getInstance(algorithmKey);

                // 初始化，设置为解密模式
                cipher.init(Cipher.DECRYPT_MODE, k);

                // 执行操作
                return cipher.doFinal(data);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 转换为16进制
         *
         * @param data
         * @return
         */
        public static String toHex(byte[] data) {
            return Hex.encodeHexString(data);
        }

        /**
         * 解析16进制字符串
         *
         * @param hex
         * @return
         */
        public static byte[] parseHex(String hex) {
            try {
                return Hex.decodeHex(hex.toCharArray());
            } catch (DecoderException e) {
                throw new RuntimeException(e);
            }
        }

}
