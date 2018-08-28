package com.pepper.common.ftp;
import java.io.Serializable;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/26
 * @Description:FTP/SFTP 配置类
 */
public class FtpConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ftp/sftp主机host
     */
    private String host;
    /**
     * 端口
     */
    private int port;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 密码
     */
    private String passWord;
    /**
     * 字符编码集
     */
    private String encoding = "UTF-8";

    /**
     * socket超时时间 默认60s
     */
    private int connetionTimeOutInMills = 1000 * 60;

    /**
     * 读取数据超时间默认60s
     */
    private int dataTimeoutInMills = 1000 * 60;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public int getConnetionTimeOutInMills() {
        return connetionTimeOutInMills;
    }

    public void setConnetionTimeOutInMills(int connetionTimeOutInMills) {
        this.connetionTimeOutInMills = connetionTimeOutInMills;
    }

    public int getDataTimeoutInMills() {
        return dataTimeoutInMills;
    }

    public void setDataTimeoutInMills(int dataTimeoutInMills) {
        this.dataTimeoutInMills = dataTimeoutInMills;
    }
}
