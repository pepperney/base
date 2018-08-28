package com.pepper.common.crypto;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/26
 * @Description:数字签名
 */
public class Signature {

    // 算法
    public static enum Algorithm {
        RSA("RSA", 1024),
        DSA("DSA", 1024),
        ECDSA("ECDSA", 256);

        private String name; // 名称
        private int keySize; // 密钥长度

        private Algorithm(String name, int keySize) {
            this.name = name;
            this.keySize = keySize;
        }

        public String getName() {
            return name;
        }

        public int getKeySize() {
            return keySize;
        }

        @Override
        public String toString() {
            return this.name + "_" + this.keySize;
        }

        /**
         * 根据名称获取对应的算法
         *
         * @param name
         * @return
         */
        public static Algorithm get(String name, int length) {
            Algorithm defaultAlgorithm = null;
            for (Algorithm algorithm : Algorithm.values()) {
                if (algorithm.getName().equals(name)) {
                    if (defaultAlgorithm == null) {
                        defaultAlgorithm = algorithm;
                    }

                    if (algorithm.getKeySize() == length) {
                        return algorithm;
                    }
                }
            }
            return defaultAlgorithm;
        }
    }

    // 签名算法
    public static enum SignMode {
        MD5withRSA("MD5withRSA"),
        SHA1withRSA("SHA1withRSA"),
        SHA256withRSA("SHA256withRSA"),
        SHA384withRSA("SHA384withRSA"),
        SHA512withRSA("SHA512withRSA"),

        SHA1withDSA("SHA1withDSA"),
        SHA224withDSA("SHA224withDSA"),
        SHA256withDSA("SHA256withDSA"),
        SHA2384withDSA("SHA2384withDSA"),
        SHA512withDSA("SHA512withDSA"),

        SHA1withECDSA("SHA1withECDSA"),
        SHA224withECDSA("SHA224withECDSA"),
        SHA256withECDSA("SHA256withECDSA"),
        SHA2384withECDSA("SHA2384withECDSA"),
        SHA512withECDSA("SHA512withECDSA");

        private String name; // 名称

        private SignMode(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return this.name;
        }

        /**
         * 根据名称获取对应的签名算法
         *
         * @param name
         * @return
         */
        public static SignMode get(String name) {
            return SignMode.valueOf(name);
        }
    }
}
