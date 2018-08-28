package com.pepper.common.ftp;

import com.pepper.common.exception.BaseException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;


/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/26
 * @Description:
 */
@Data
@Slf4j
public class Ftp implements Serializable {

    private static final long serialVersionUID = 1L;
    private String host;
    private int port = 21;
    private String username;
    private String password;
    private String encoding = "UTF-8";
    private int socketTimeout = 1000 * 60;
    /**
     * socket超时时间 默认60s
     */
    private int connectTimeout = 1000 * 60;
    /**
     * 数据读取超时时间 默认60s
     */
    private int dataTimeout = 1000 * 60;

    private FTPClient ftp = null;

    public Ftp(FtpConfig ftpCfg) {
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
        if (StringUtils.isNotBlank(ftpCfg.getEncoding())) {
            this.encoding = ftpCfg.getEncoding();
        }
        this.username = ftpCfg.getUserName();
        this.password = ftpCfg.getPassWord();

        if (ftpCfg.getConnetionTimeOutInMills() > 0) {
            this.socketTimeout = ftpCfg.getConnetionTimeOutInMills();
            this.connectTimeout = ftpCfg.getConnetionTimeOutInMills();
        }
        if (ftpCfg.getDataTimeoutInMills() > 0) {
            this.dataTimeout = ftpCfg.getDataTimeoutInMills();
        }
    }

    protected void connect() throws BaseException {
        try {
            if (ftp != null) {
                log.info("ftp is not null");
            } else {
                ftp = new FTPClient();
            }
            ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
            ftp.setDefaultPort(getPort());
            ftp.connect(getHost());
            ftp.setReceiveBufferSize(Integer.MAX_VALUE);// 不限制socket
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                log.warn("FTP server refused connection: " + getHost());
                ftp.disconnect();
                throw new BaseException(FTPEnum.NOCONNECTED.getCode(), host + ",FTP拒绝连接:" + getHost());
            }
            if (!ftp.login(getUsername(), getPassword())) {
                log.warn("FTP server refused login: " + getHost() + ", user: " + getUsername());
                ftp.logout();
                ftp.disconnect();
                throw new BaseException(FTPEnum.NOCONNECTED.getCode(), host + ",FTP登录失败,userName:" + getUsername() + ",passwod:" + getPassword());
            }
            ftp.setControlEncoding(getEncoding());
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();
            ftp.setDefaultTimeout(socketTimeout);
            ftp.setConnectTimeout(connectTimeout);
            ftp.setSoTimeout(socketTimeout);
            ftp.setDataTimeout(dataTimeout);
            ftp.setControlEncoding(encoding);

        } catch (SocketException e) {
            log.error("host {} connect error", this.host, e);
            throw new BaseException(FTPEnum.NOCONNECTED.getCode(), host + ",FTP连接Socket异常:" + e.getMessage(), e);
        } catch (IOException e) {
            log.error("host {} connect error", this.host, e);
            throw new BaseException(FTPEnum.NOCONNECTED.getCode(), host + ",FTP连接IO异常:" + e.getMessage(), e);
        }
    }

    protected void disconnect() throws BaseException {
        if (ftp != null) {
            try {
                ftp.logout();
                ftp.disconnect();
            } catch (IOException e) {
                log.error("ftp close error", e);
            }
        } else {
            log.info("ftp is closed");
        }
    }

    /**
     * 上传文件
     *
     * @param remoteFileName
     * @param file
     * @return
     * @throws BaseException
     */
    @SuppressWarnings("deprecation")
    public boolean upload(String remoteFileName, File file) throws BaseException {
        InputStream in = null;
        this.connect();
        try {
            if (ftp != null) {
                in = FileUtils.openInputStream(file);
                remoteFileName = FilenameUtils.separatorsToUnix(remoteFileName);
                if (!ftp.changeWorkingDirectory(FilenameUtils.getFullPathNoEndSeparator(remoteFileName))) {
                    createRemoteFolder(FilenameUtils.getFullPathNoEndSeparator(remoteFileName));
                }

                String remoteFile = new String(FilenameUtils.getName(remoteFileName).getBytes(this.encoding), "ISO-8859-1");
                ftp.storeFile(remoteFile, in);
            }
        } catch (Exception e) {
            log.error("host {} upload error", this.host, e);
            throw new BaseException(FTPEnum.FILEEXCEPTION.getCode(), host + "," + remoteFileName + ",FTP异常:" + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(in);
            this.disconnect();
        }
        return true;
    }

    private void createRemoteFolder(String remotePath) throws IOException {
        String[] folders = remotePath.split("/");
        StringBuffer remoteTempPath = new StringBuffer("");
        for (String folder : folders) {
            if (StringUtils.isNotBlank(folder)) {
                remoteTempPath.append("/" + folder);
                boolean flag = ftp.changeWorkingDirectory(remoteTempPath.toString());
                if (!flag) {
                    flag = ftp.makeDirectory(remoteTempPath.toString());
                    ftp.changeWorkingDirectory(remoteTempPath.toString());
                }
            }
        }
    }

    /**
     * 获取当前目录下所有的文件名
     *
     * @param remoteURL
     * @return
     */
    public List<String> getFileNames(String remoteURL) throws BaseException {
        this.connect();
        List<String> fileNames = new ArrayList<>();

        try {
            FTPFile[] files = ftp.listFiles(FilenameUtils.getName(remoteURL));
            for (FTPFile file : files) {
                fileNames.add(file.getName());
            }
        } catch (Exception e) {
            log.error("host {} getFileNames error", this.host, e);
            throw new BaseException(FTPEnum.FILEEXCEPTION.getCode(), host + "," + remoteURL + ",FTP异常:" + e.getMessage(), e);
        } finally {
            this.disconnect();
        }

        return fileNames;
    }

    /**
     * 下载远程FTP文件
     *
     * @param remoteFullPathFile
     * @param localFullPathFile
     * @return
     */
    public void download(String remoteFullPathFile, String localFullPathFile) throws BaseException {
        this.connect();
        try {
            FTPFile[] files = ftp.listFiles(new String(remoteFullPathFile.getBytes(this.encoding), "ISO-8859-1"));
            if (1 != files.length) {
                throw new BaseException(FTPEnum.NOSUCHFILE.getCode(), "文件FTP中不存在");
            }
            FileUtils.copyInputStreamToFile(ftp.retrieveFileStream(remoteFullPathFile), new File(localFullPathFile));
        } catch (Exception e) {
            log.error("host {} download error", this.host, e);
            throw new BaseException(FTPEnum.FILEEXCEPTION.getCode(), host + "," + remoteFullPathFile + ",FTP异常:" + e.getMessage(), e);
        } finally {
            this.disconnect();
        }
    }

}
