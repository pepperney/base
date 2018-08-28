package com.pepper.common.util;

import com.pepper.common.crypto.Digest;
import com.pepper.common.crypto.Hmac;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/26
 * @Description:信息摘要工具
 */
public class DigestUtil {
    /**
     * 获取信息摘要实例
     *
     * @param algorithm
     * @return
     */
    public static MessageDigest getMessageDigest(Digest algorithm) {
        switch (algorithm) {
            case MD2:
                return DigestUtils.getMd2Digest();
            case MD5:
                return DigestUtils.getMd5Digest();
            case SHA1:
                return DigestUtils.getSha1Digest();
            case SHA256:
                return DigestUtils.getSha256Digest();
            case SHA384:
                return DigestUtils.getSha384Digest();
            case SHA512:
                return DigestUtils.getSha512Digest();
            default:
                throw new UnsupportedOperationException("algorithm: " + algorithm);
        }
    }

    /**
     * 对byte数组进行md5摘要
     *
     * @param data
     * @return
     */
    public static byte[] digest(Digest algorithm, byte[] data) {
        switch (algorithm) {
            case MD2:
                return DigestUtils.md2(data);
            case MD5:
                return DigestUtils.md5(data);
            case SHA1:
                return DigestUtils.sha1(data);
            case SHA256:
                return DigestUtils.sha256(data);
            case SHA384:
                return DigestUtils.sha384(data);
            case SHA512:
                return DigestUtils.sha512(data);
            default:
                throw new UnsupportedOperationException("algorithm: " + algorithm);
        }
    }

    /**
     * 对String使用utf-8编码后进行md5摘要
     *
     * @param data
     * @return
     */
    public static byte[] digest(Digest algorithm, String data) {
        switch (algorithm) {
            case MD2:
                return DigestUtils.md2(data);
            case MD5:
                return DigestUtils.md5(data);
            case SHA1:
                return DigestUtils.sha1(data);
            case SHA256:
                return DigestUtils.sha256(data);
            case SHA384:
                return DigestUtils.sha384(data);
            case SHA512:
                return DigestUtils.sha512(data);
            default:
                throw new UnsupportedOperationException("algorithm: " + algorithm);
        }
    }

    /**
     * 对输入流进行md5摘要
     *
     * @param data
     * @return
     */
    public static byte[] digest(Digest algorithm, InputStream data) {
        try {
            switch (algorithm) {
                case MD2:
                    return DigestUtils.md2(data);
                case MD5:
                    return DigestUtils.md5(data);
                case SHA1:
                    return DigestUtils.sha1(data);
                case SHA256:
                    return DigestUtils.sha256(data);
                case SHA384:
                    return DigestUtils.sha384(data);
                case SHA512:
                    return DigestUtils.sha512(data);
                default:
                    throw new UnsupportedOperationException("algorithm: " + algorithm);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 对byte数组进行md5摘要，返回16进制字符串
     *
     * @param data
     * @return
     */
    public static String digestHex(Digest algorithm, byte[] data) {
        switch (algorithm) {
            case MD2:
                return DigestUtils.md2Hex(data);
            case MD5:
                return DigestUtils.md5Hex(data);
            case SHA1:
                return DigestUtils.sha1Hex(data);
            case SHA256:
                return DigestUtils.sha256Hex(data);
            case SHA384:
                return DigestUtils.sha384Hex(data);
            case SHA512:
                return DigestUtils.sha512Hex(data);
            default:
                throw new UnsupportedOperationException("algorithm: " + algorithm);
        }
    }

    /**
     * 对String使用utf-8编码后进行md5摘要，返回16进制字符串
     *
     * @param data
     * @return
     */
    public static String digestHex(Digest algorithm, String data) {
        switch (algorithm) {
            case MD2:
                return DigestUtils.md2Hex(data);
            case MD5:
                return DigestUtils.md5Hex(data);
            case SHA1:
                return DigestUtils.sha1Hex(data);
            case SHA256:
                return DigestUtils.sha256Hex(data);
            case SHA384:
                return DigestUtils.sha384Hex(data);
            case SHA512:
                return DigestUtils.sha512Hex(data);
            default:
                throw new UnsupportedOperationException("algorithm: " + algorithm);
        }
    }

    /**
     * 对输入流进行md5摘要，返回16进制字符串
     *
     * @param data
     * @return
     */
    public static String digestHex(Digest algorithm, InputStream data) {
        try {
            switch (algorithm) {
                case MD2:
                    return DigestUtils.md2Hex(data);
                case MD5:
                    return DigestUtils.md5Hex(data);
                case SHA1:
                    return DigestUtils.sha1Hex(data);
                case SHA256:
                    return DigestUtils.sha256Hex(data);
                case SHA384:
                    return DigestUtils.sha384Hex(data);
                case SHA512:
                    return DigestUtils.sha512Hex(data);
                default:
                    throw new UnsupportedOperationException("algorithm: " + algorithm);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成给定HMAC算法的密钥
     *
     * @param hmac
     * @return
     */
    public static byte[] generateHmacKey(Hmac hmac) {
        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance(hmac.getName());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        // 生成密钥
        SecretKey secretKey = keyGen.generateKey();

        // 获取密钥
        return secretKey.getEncoded();
    }

    /**
     * 对byte数组进行HMAC摘要
     *
     * @param data
     * @param hmac
     * @param key
     * @return
     */
    public static byte[] digestHmac(byte[] data, Hmac hmac, byte[] key) {
        // 还原密钥
        SecretKey secretKey = new SecretKeySpec(key, hmac.getName());

        try {
            // 实例化Mac
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());

            // 初始化Mac
            mac.init(secretKey);

            // 执行信息摘要
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 对字符串使用utf-8编码后进行HMAC摘要
     *
     * @param data
     * @param hmac
     * @param key
     * @return
     */
    public static byte[] digestHmac(String data, Hmac hmac, byte[] key) {
        return digestHmac(StringUtils.getBytesUtf8(data), hmac, key);
    }

    /**
     * 对byte数组进行HMAC摘要, 返回16进制字符串
     *
     * @param data
     * @param hmac
     * @param key
     * @return
     */
    public static String digestHmacHex(byte[] data, Hmac hmac, byte[] key) {
        return Hex.encodeHexString(digestHmac(data, hmac, key));
    }

    /**
     * 对字符串使用utf-8编码后进行HMAC摘要, 返回16进制字符串
     *
     * @param data
     * @param hmac
     * @param key
     * @return
     */
    public static String digestHmacHex(String data, Hmac hmac, byte[] key) {
        return Hex.encodeHexString(digestHmac(data, hmac, key));
    }

}
