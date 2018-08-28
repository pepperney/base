package com.pepper.common.util;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import com.pepper.common.crypto.RSAKey;
import com.pepper.common.crypto.Signature;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/26
 * @Description:
 */

public class SignatureUtil {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 生成密钥
     *
     * @param algorithm 算法
     * @param keySize   密钥长度，为0则使用算法默认长度
     * @return
     */
    public static RSAKey generateKey(Signature.Algorithm algorithm) {
        return generateKey(algorithm, 0);
    }

    /**
     * 生成密钥
     *
     * @param algorithm 算法
     * @param keySize   密钥长度，为0则使用算法默认长度
     * @return
     */
    public static RSAKey generateKey(Signature.Algorithm algorithm, int keySize) {
        KeyPairGenerator keyPairGen = null;
        try {
            keyPairGen = KeyPairGenerator.getInstance(algorithm.getName());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // 初始化
        keySize = keySize > 0 ? keySize : algorithm.getKeySize();
        keyPairGen.initialize(keySize);

        // 生成密钥对
        KeyPair keyPair = keyPairGen.generateKeyPair();

        RSAKey rsaKey = new RSAKey();

        // 公钥
        rsaKey.setPublicKey(keyPair.getPublic().getEncoded());
        // 私钥
        rsaKey.setPrivateKey(keyPair.getPrivate().getEncoded());

        return rsaKey;
    }

    /**
     * 用私钥对数据进行签名
     *
     * @param data
     * @param algorithm
     * @param signMode
     * @param privateKey
     * @return
     */
    public static byte[] sign(byte[] data, Signature.Algorithm algorithm, Signature.SignMode signMode,
                              byte[] privateKey) {
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKey);

        try {
            // 实例化密钥工厂
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm.getName());
            PrivateKey key = keyFactory.generatePrivate(pkcs8KeySpec);

            // 实例化Signature
            java.security.Signature signature = java.security.Signature.getInstance(signMode.getName());

            // 初始化签名
            signature.initSign(key);

            // 更新数据
            signature.update(data);

            // 签名
            return signature.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 用公钥对数据签名进行校验
     *
     * @param data
     * @param algorithm
     * @param signMode
     * @param publicKey
     * @param sign
     * @return
     */
    public static boolean verify(byte[] data, Signature.Algorithm algorithm, Signature.SignMode signMode,
                                 byte[] publicKey, byte[] sign) {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);

        try {
            // 实例化密钥工厂
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm.getName());

            // 生成公钥
            PublicKey key = keyFactory.generatePublic(keySpec);

            // 实例化Signature
            java.security.Signature signature = java.security.Signature.getInstance(signMode.getName());

            // 初始化签名
            signature.initVerify(key);

            // 更新数据
            signature.update(data);

            // 验证
            return signature.verify(sign);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
