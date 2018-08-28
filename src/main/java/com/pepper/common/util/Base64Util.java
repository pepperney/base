package com.pepper.common.util;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;


/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/26
 * @Description:BASE64编码工具类
 */
public class Base64Util {
    /**
     * 编码成base64
     *
     * @param data
     * @return
     */
    public static String encode(byte[] data) {
        return Base64.encodeBase64String(data);
    }

    /**
     * 编码成base64
     *
     * @param data
     * @return
     */
    public static String encode(String data) {
        return Base64.encodeBase64String(StringUtils.getBytesUtf8(data));
    }

    /**
     * 将base64字符串解码为byte数组
     *
     * @param data
     * @return
     */
    public static byte[] decode(String data) {
        return Base64.decodeBase64(data);
    }

    /**
     * 将base64字符串解码为字符串
     *
     * @param data
     * @return
     */
    public static String decodeToString(String data) {
        byte[] result = decode(data);
        return StringUtils.newStringUtf8(result);
    }

    /**
     * 编码成base64的URL安全格式
     *
     * @param data
     * @return
     */
    public static String encodeUrlSafe(byte[] data) {
        return StringUtils.newStringUtf8(Base64.encodeBase64URLSafe(data));
    }

}
