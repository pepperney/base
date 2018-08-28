package com.pepper.common.crypto;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/26
 * @Description:RSA密钥
 */
public class RSAKey{
        private byte[] publicKey; // 公钥
        private byte[] privateKey; // 私钥

        /**
         * 获取公钥
         *
         * @return
         */
        public byte[] getPublicKey() {
            return publicKey;
        }

        /**
         * 设置公钥
         *
         * @param publicKey
         */
        public void setPublicKey(byte[] publicKey) {
            this.publicKey = publicKey;
        }

        /**
         * 获取私钥
         *
         * @return
         */
        public byte[] getPrivateKey() {
            return privateKey;
        }

        /**
         * 设置私钥
         *
         * @param privateKey
         */
        public void setPrivateKey(byte[] privateKey) {
            this.privateKey = privateKey;
        }

}
