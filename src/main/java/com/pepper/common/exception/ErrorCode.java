package com.pepper.common.exception;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/26
 * @Description:
 */
public enum ErrorCode {

    SUCCESS(0x0, "成功"),
    ERR_UNKNOWN(0x40000001, "未知异常"),
    ERR_SYSTEM(0x40000002, "系统异常"),
    ERR_PARAM(0x40000003, "参数异常"),
    ERR_TIMEOUT(0x40000004, "处理超时"),
    ERR_TIMEOUT_CONNECT(0x40000005, "连接超时"),
    ERR_TIMEOUT_SOCKET(0x40000006, "链接超时");

    private final int value;

    private final String message;

    ErrorCode(int value, String message) {
        this.value = value;
        this.message = message;
    }

    public int getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    public String getCode() {
        return Integer.toHexString(this.value).toUpperCase();
    }

    public String getSystem() {
        return Integer.toHexString(this.value >>> 24).toUpperCase();
    }

    public String getModule() {
        return Integer.toHexString(this.value >>> 16).toUpperCase();
    }

    public static ErrorCode getByCode(int value) {
        for (ErrorCode _enum : values()) {
            if (_enum.getValue() == value) {
                return _enum;
            }
        }
        return null;
    }

}
