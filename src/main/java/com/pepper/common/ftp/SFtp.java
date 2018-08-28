package com.pepper.common.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.pepper.common.exception.BaseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/26
 * @Description:
 */
@Slf4j
@Data
public class SFtp {

    /** ip地址 */
    private String host;
    /** 用户名 */
    private String username;
    /** 密码 */
    private String password;
    /** 端口 */
    private int port = 22;
    /**
     * 建立socket 连接超时时间
     */
    private int connetionTimeOutInMills = 60 * 1000;
    private ChannelSftp sftp = null;
    Session sshSession = null;

    public SFtp(FtpConfig ftpCfg) {
        if (StringUtils.isEmpty(ftpCfg.getHost())) {
            throw new IllegalArgumentException("host 不能为空");
        }
        if (StringUtils.isEmpty(ftpCfg.getUserName())) {
            throw new IllegalArgumentException("用户名 不能为空");
        }
        if (StringUtils.isEmpty(ftpCfg.getPassWord())) {
            throw new IllegalArgumentException("密码 不能为空");
        }
        this.host = ftpCfg.getHost();
        if (0 != ftpCfg.getPort()) {
            this.port = ftpCfg.getPort();
        }
        this.username = ftpCfg.getUserName();
        this.password = ftpCfg.getPassWord();
        if (ftpCfg.getConnetionTimeOutInMills() > 0) {
            this.connetionTimeOutInMills = ftpCfg.getConnetionTimeOutInMills();
        }
    }

    /**
     * 获取连接
     *
     * @Method: connect
     * @Description: 获取连接
     * @throws BaseException
     */
    protected void connect() throws BaseException {
        try {
            if (sftp != null) {
                log.info("sftp is not null");
            }
            JSch jsch = new JSch();
            sshSession = jsch.getSession(username, host, port);
            sshSession.setPassword(password);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setTimeout(connetionTimeOutInMills);
            sshSession.setConfig(sshConfig);
            sshSession.connect();
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            log.info("host {} sftp connect success", host);
            sftp = (ChannelSftp) channel;
        } catch (Exception e) {
            log.error("host {} connect error", this.host, e);
            throw new BaseException(FTPEnum.NOCONNECTED.getCode(), host + ",连接SFTP失败:" + e.getMessage(), e);
        }
    }

    /**
     * 关闭连接
     *
     * @Method: disconnect
     * @Description: 关闭连接
     * @throws BaseException
     */
    protected void disconnect() throws BaseException {
        if (this.sftp != null) {
            if (this.sftp.isConnected()) {
                this.sftp.disconnect();
            } else if (this.sftp.isClosed()) {
                log.info("sftp is closed already");
            }
        }
        if (this.sshSession != null) {
            if (this.sshSession.isConnected()) {
                this.sshSession.disconnect();
            }
        }
    }

    /**
     * 获取指定远程路径的所有的文件名
     *
     * @param remoteURL
     * @return
     * @throws BaseException
     */
    @SuppressWarnings("unchecked")
    public List<String> getFileNames(String remoteURL) throws BaseException {
        this.connect();
        List<String> fileNames = new ArrayList<>();
        List<LsEntry> fileList = new ArrayList<LsEntry>();
        try {
            Vector<LsEntry> vct = sftp.ls(remoteURL);
            if (!vct.isEmpty()) {

                for (int i = 0; i < vct.size(); i++) {
                    fileList.add(vct.get(i));
                }
            }

            for (LsEntry file : fileList) {
                fileNames.add(file.getFilename());
            }
        } catch (SftpException e) {
            log.error("host {} getFileNames error", this.host, e);
            throw new BaseException(FTPEnum.FILEEXCEPTION.getCode(), host + "," + remoteURL + ",SFTP 异常:" + e.getMessage(), e);
        } finally {
            this.disconnect();
        }
        return fileNames;
    }

    /**
     * 下载文件
     *
     * @Method: download
     * @Description: 下载文件
     * @param remoteFullPathFile
     * @return
     * @throws BaseException
     */
    @SuppressWarnings("deprecation")
    public void download(String remoteFullPathFile, String localFullPathFile) throws BaseException {
        this.connect();

        File file = null;
        FileOutputStream fos = null;
        try {

            file = new File(localFullPathFile);
            fos = new FileOutputStream(file);

            sftp.get(remoteFullPathFile, fos);

        } catch (Exception e) {
            log.error("host {} download error", this.host, e);
            throw new BaseException(FTPEnum.FILEEXCEPTION.getCode(), host + "," + remoteFullPathFile + ",SFTP下载失败:" + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(fos);
            this.disconnect();
        }
    }



    private void createRemoteFolder(String remotePath) throws SftpException {
        String[] folders = remotePath.split("/");
        StringBuffer remoteTempPath = new StringBuffer("");
        for (String folder : folders) {
            if (StringUtils.isNotBlank(folder)) {
                remoteTempPath.append("/" + folder);
                if (!dirExist(remoteTempPath.toString())) { // 目录不存在，创建并cd到该目录
                    sftp.mkdir(remoteTempPath.toString());
                }
            }
        }
    }

    /**
     * 上传文件
     *
     * @Method: upload
     * @Description: 上传文件
     * @param remoteFullPathFile
     * @param in
     * @return
     * @throws BaseException
     */
    @SuppressWarnings("deprecation")
    public boolean upload(String remoteFullPathFile, File file) throws BaseException {
        this.connect();
        InputStream in = null;
        try {
            remoteFullPathFile = FilenameUtils.separatorsToUnix(remoteFullPathFile);
            if (!dirExist(FilenameUtils.getFullPathNoEndSeparator(remoteFullPathFile))) {
                createRemoteFolder(FilenameUtils.getFullPathNoEndSeparator(remoteFullPathFile));
            }
            in = new FileInputStream(file);
            this.sftp.put(in, remoteFullPathFile);
        } catch (Exception e) {
            log.error("host {} upload error", this.host, e);
            throw new BaseException(FTPEnum.FILEEXCEPTION.getCode(), host + "," + remoteFullPathFile + ",SFTP上传失败:" + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(in);
            this.disconnect();
        }
        return true;
    }

    /**
     * 验证目录是否存在
     *
     * @param dir
     * @return
     */
    private boolean dirExist(final String dir) {
        try {
            Vector<?> vector = sftp.ls(dir);
            if (null == vector) {
                return false;
            } else {
                return true;
            }
        } catch (SftpException e) {
            log.error("sftp ls error ", e);
            return false;
        }
    }

}
