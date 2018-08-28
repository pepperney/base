package com.pepper.common.crypto;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/26
 * @Description:HMAC算法
 */
public enum Hmac {

    HmacMD5("HmacMD5"),
    HmacSHA1("HmacSHA1"),
    HmacSHA256("HmacSHA256"),
    HmacSHA384("HmacSHA384"),
    HmacSHA512("HmacSHA512");

    private String name; // 算法名称

    private Hmac(String name) {
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
     * 根据名称获取对应的HMAC算法
     *
     * @param name
     * @return
     */
    public static Hmac get(String name) {
        return Hmac.valueOf(name);
    }

}
