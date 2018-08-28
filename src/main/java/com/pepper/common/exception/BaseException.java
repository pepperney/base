package com.pepper.common.exception;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/26
 * @Description:
 */
public class BaseException extends RuntimeException{

    private static final long serialVersionUID = -8777276057702845371L;

    /** 系统编码*/
    private String systemCode;

    /** 异常编码*/
    private String errorCode;

    public BaseException(String errorCode, String message){
        super(message);
        this.errorCode = errorCode;
    }

    public BaseException(String errorCode, String message, Throwable e){
        super(message, e);
        this.errorCode = errorCode;
    }

    public BaseException(String errorCode, String message, String systemCode){
        super(message);
        this.errorCode = errorCode;
        this.systemCode = systemCode;
    }

    public BaseException(String errorCode, String message, String systemCode, Throwable e){
        super(message, e);
        this.errorCode = errorCode;
        this.systemCode = systemCode;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public String getSystemCode() {
        return systemCode;
    }
}
