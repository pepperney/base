package com.pepper.common.crypto;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/26
 * @Description:加密解密定义值
 */
public class Crypto {
    public static enum Algorithm {

        // DES
        DES("DES", 56),

        // 3DES
        DESede112("DESede", 112),
        DESede168("DESede", 168),

        // AES
        AES128("AES", 128),
        AES192("AES", 192), // 因出口限制，默认情况下JDK不支持该长度，需修改policy文件
        AES256("AES", 256); // 因出口限制，默认情况下JDK不支持该长度，需修改policy文件

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
            return this.name + "-" + this.keySize;
        }

        /**
         * 根据名称获取对应的工作模式
         *
         * @param name
         * @return
         */
        public static Algorithm get(String name, int length) {
            for (Algorithm algorithm : Algorithm.values()) {
                if (algorithm.getName().equals(name) && algorithm.getKeySize() == length) {
                    return algorithm;
                }
            }

            throw new UnsupportedOperationException("Can't find Algorithm by name: "
                    + name + ", keySize: " + length);
        }
    }

    // 工作模式
    public static enum Mode {
        ECB("ECB"),
        CBC("CBC"),
        CFB("CFB"),
        PCBC("PCBC"),
        CTR("CTR"),
        CTS("CTS"),
        OFB("OFB");

        private String name; // 名称

        private Mode(String name) {
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
         * 根据名称获取对应的工作模式
         *
         * @param name
         * @return
         */
        public static Mode get(String name) {
            return Mode.valueOf(name);
        }
    }

    // 对齐方式
    public static enum Padding {
        NoPadding("NoPadding"),
        PKCS5Padding("PKCS5Padding"),
        ISO10126Padding("ISO10126Padding");

        private String name; // 名称

        private Padding(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return this.name;
        }

        public static Padding get(String name) {
            return Padding.valueOf(name);
        }
    }

}
