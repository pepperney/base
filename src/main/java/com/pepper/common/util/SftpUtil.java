package com.pepper.common.util;

import com.pepper.common.exception.BaseException;
import com.pepper.common.ftp.FtpConfig;
import com.pepper.common.ftp.SFtp;

import java.io.File;
import java.util.List;


/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/26
 * @Description:sftp下载工具类
 */
public class SftpUtil {

    /**
     * 上传文件到远程SFTP
     *
     * @param config             SFTP 配置信息
     * @param remoteFullPathFile 文件上传远程完整的路径
     * @param file               需要上传的文件
     * @return 上传成功返回 true
     * @throws BaseException
     */
    public static boolean upload(FtpConfig config, String remoteFullPathFile, File file) throws BaseException {
        SFtp sftp = new SFtp(config);
        return sftp.upload(remoteFullPathFile, file);
    }

    /**
     * 获取远程SFTP文件列表
     *
     * @param config    SFTP 配置信息
     * @param remoteURL 指定的远程文件路径地址
     * @return 文件名列表集合
     * @throws BaseException
     */
    public static List<String> getFileNames(FtpConfig config, String remoteURL) throws BaseException {
        SFtp sftp = new SFtp(config);
        return sftp.getFileNames(remoteURL);
    }

    /**
     * @param config             SFTP 配置信息
     * @param remoteFullPathFile 远程文件完整的路径
     * @param localFullPathFile  文件下载到的本地完整路径
     */
    public static void download(FtpConfig config, String remoteFullPathFile, String localFullPathFile) {
        SFtp sftp = new SFtp(config);
        sftp.download(remoteFullPathFile, localFullPathFile);
    }

}
