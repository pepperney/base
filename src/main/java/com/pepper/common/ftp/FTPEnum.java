package com.pepper.common.ftp;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/26
 * @Description:
 */
public enum FTPEnum {
    IOEXCEPTION("IOEXCEPTION", "IO异常"),
    SOCKETEXCEPTION("SOCKETEXCEPTION", "SOCKET异常"),
    NOSUCHFILE("NOSUCHFILE", "找不到指定文件或文件夹"),
    NOCONNECTED("NOCONNECTED", "连接服务器失败"),
    FILEEXCEPTION("FILEEXCEPTION", "文件操作过程中失败");
    private String code;
    private String msg;
    private boolean display;

    FTPEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
        this.display = true;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    /**
     * 根据枚举的code返回枚举对象
     *
     * @param code 枚举code值
     * @return 枚举对象
     */
    public static FTPEnum getEnumByCode(String code) {
        if (code == null) return null;
        for (FTPEnum type : values()) {
            if (type.getCode().equals(code.trim()))
                return type;
        }
        return null;
    }

    /**
     * 将枚举转换成code-msg形式的集合
     * 可通过<code>EnumXXX.setDisplay(false);</code>的方式将不需要的枚举类型不转换成map
     *
     * @return 转换后的map集合
     */
    public static Map<String, String> toMap() {
        Map<String, String> enumDataMap = new LinkedHashMap<String, String>();
        for (FTPEnum type : FTPEnum.values()) {
            if (type.isDisplay()) enumDataMap.put(type.getCode(), type.getMsg());
        }
        return enumDataMap;
    }

    /**
     * 将枚举转换成code-code-msg形式的集合
     * 可通过<code>EnumXXX.setDisplay(false);</code>的方式将不需要的枚举类型不转换成map
     *
     * @return 转换后的map集合
     */
    public static Map<String, String> toMixMap() {
        Map<String, String> enumDataMap = new LinkedHashMap<String, String>();
        for (FTPEnum type : FTPEnum.values()) {
            if (type.isDisplay()) enumDataMap.put(type.getCode(), type.getCode() + "-" + type.getMsg());
        }
        return enumDataMap;
    }


}
