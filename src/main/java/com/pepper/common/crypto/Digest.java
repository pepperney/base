package com.pepper.common.crypto;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/26
 * @Description:信息摘要算法
 */
public enum Digest {
    MD2("MD2"),
    MD5("MD5"),
    SHA1("SHA1"),
    SHA256("SHA256"),
    SHA384("SHA384"),
    SHA512("SHA512");

    private String name; // 名称

    private Digest(String name) {
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
     * 根据名称获取对应的信息摘要算法
     *
     * @param name
     * @return
     */
    public static Digest get(String name) {
        return Digest.valueOf(name);
    }

}
