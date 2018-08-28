package com.pepper.common.util;

import com.pepper.common.exception.BaseException;
import com.pepper.common.ftp.Ftp;
import com.pepper.common.ftp.FtpConfig;

import java.io.File;
import java.util.List;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/26
 * @Description:
 */
public class FtpUtil {

    /**
     * 上传文件到远程FTP
     *
     * @param config             FTP 配置信息
     * @param remoteFullPathFile 文件上传远程完整的路径
     * @param file               需要上传的文件
     * @return 上传成功返回 true
     * @throws BaseException
     */
    public static boolean upload(FtpConfig config, String remoteFullPathFile, File file) throws BaseException {
        Ftp ftp = new Ftp(config);
        return ftp.upload(remoteFullPathFile, file);
    }

    /**
     * 获取远程FTP文件列表
     *
     * @param config    FTP 配置信息
     * @param remoteURL 指定的远程文件路径地址
     * @return 文件名列表集合
     * @throws BaseException
     */
    public static List<String> getFileNames(FtpConfig config, String remoteURL) throws BaseException {
        Ftp ftp = new Ftp(config);
        return ftp.getFileNames(remoteURL);
    }

    /**
     * @param config             FTP 配置信息
     * @param remoteFullPathFile 远程文件完整的路径
     * @param localFullPathFile  文件下载到的本地完整路径
     */
    public static void download(FtpConfig config, String remoteFullPathFile, String localFullPathFile) {
        Ftp ftp = new Ftp(config);
        ftp.download(remoteFullPathFile, localFullPathFile);
    }

}
