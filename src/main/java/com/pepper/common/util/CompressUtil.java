package com.pepper.common.util;

import com.alibaba.fastjson.util.IOUtils;
import com.pepper.common.exception.ErrorCode;
import com.pepper.common.exception.BaseException;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/26
 * @Description:Java文件 文件目录压缩解压
 */
public class CompressUtil {

    private static final Logger log = LoggerFactory.getLogger(CompressUtil.class);

    /**
     * 递归取到当前目录所有文件
     *
     * @param dir
     * @return
     */
    private static List<String> getFiles(String dir) {
        List<String> lstFiles = null;
        if (lstFiles == null) {
            lstFiles = new ArrayList<String>();
        }
        File file = new File(dir);
        if (!file.exists()) {
            throw new IllegalArgumentException(dir + "需要压缩的目录不存在!");
        }
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                lstFiles.add(f.getAbsolutePath());
                lstFiles.addAll(getFiles(f.getAbsolutePath()));
            } else {
                String str = f.getAbsolutePath();
                lstFiles.add(str);
            }
        }
        return lstFiles;
    }

    /**
     * 文件名处理
     *
     * @param dir
     * @param path
     * @return
     */
    private static String getFilePathName(String dir, String path) {
        String p = path.replace(dir + File.separator, "");
        p = p.replace("\\", "/");
        return p;
    }

    /**
     * 压缩文件
     *
     * @param zipFilePath 压缩文件存放路径，包含压缩文件名
     * @param dir         需要压缩的文件路径
     */
    public static void zip(String zipFilePath, String dir) {
        log.info("压缩目录 dir=[{}],压缩文件存放路径  zipFilePath = [{}]", dir, zipFilePath);
        List<String> paths = getFiles(dir); // 递归获取当前压缩目录下的所有 文件
        String[] files = paths.toArray(new String[paths.size()]);
        if (files == null || files.length <= 0) {
            return;
        }
        ZipArchiveOutputStream zaos = null;
        try {
            File zipFile = new File(zipFilePath);
            if (!zipFile.exists()) {
                throw new IllegalArgumentException(zipFilePath + "压缩文件存放路径不存在!");
            }
            zaos = new ZipArchiveOutputStream(zipFile);
            zaos.setUseZip64(Zip64Mode.AsNeeded);

            for (String strfile : files) {
                File file = new File(strfile);
                if (file != null) {
                    String name = getFilePathName(dir, strfile);
                    ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(file, name);
                    zaos.putArchiveEntry(zipArchiveEntry);
                    if (file.isDirectory()) {
                        continue;
                    }
                    InputStream is = null;
                    try {
                        is = new BufferedInputStream(new FileInputStream(file));
                        byte[] buffer = new byte[1024];
                        int len = -1;
                        while ((len = is.read(buffer)) != -1) {
                            zaos.write(buffer, 0, len);
                        }
                        zaos.closeArchiveEntry();
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                }
            }
            zaos.finish();
        } catch (Exception e) {
            log.error("压缩文件异常,zipFilePath = [{}] ,dir = [{}]", zipFilePath, dir);
            throw new BaseException(ErrorCode.ERR_SYSTEM.getCode(), "压缩文件异常", e);
        } finally {
            IOUtils.close(zaos);
        }
    }

    /**
     * 把zip文件解压到指定的文件夹
     *
     * @param zipFilePath zip文件路径, 如 "D:/test/aa.zip"
     * @param saveFileDir 解压后的文件存放路径, 如"D:/test/" ()
     */
    public static void unzip(String zipFilePath, String saveFileDir) {
        log.info("解压源文件路径 zipFilePath = [{}],  解压到 zipFilePath = [{}]", zipFilePath, saveFileDir);
        if (!saveFileDir.endsWith("\\") && !saveFileDir.endsWith("/")) {
            saveFileDir += File.separator;
        }
        File dir = new File(saveFileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(zipFilePath);
        if (!file.exists()) {
            throw new IllegalArgumentException(zipFilePath + "需要解压的文件不存在!");
        }
        InputStream is = null;
        ZipArchiveInputStream zais = null;
        try {
            is = new FileInputStream(file);
            zais = new ZipArchiveInputStream(is);
            ArchiveEntry archiveEntry = null;
            while ((archiveEntry = zais.getNextEntry()) != null) {
                String entryFileName = archiveEntry.getName();
                String entryFilePath = saveFileDir + entryFileName;
                OutputStream os = null;
                try {
                    File entryFile = new File(entryFilePath);
                    if (entryFileName.endsWith("/")) {
                        entryFile.mkdirs();
                    } else {
                        os = new BufferedOutputStream(new FileOutputStream(entryFile));
                        byte[] buffer = new byte[1024];
                        int len = -1;
                        while ((len = zais.read(buffer)) != -1) {
                            os.write(buffer, 0, len);
                        }
                    }
                } finally {
                    if (os != null) {
                        os.flush();
                        os.close();
                    }
                }

            }
        } catch (Exception e) {
            log.error("解压文件异常,zipFilePath = [{}]", zipFilePath);
            throw new BaseException(ErrorCode.ERR_SYSTEM.getCode(), "解压文件异常", e);
        } finally {
            IOUtils.close(zais);
            IOUtils.close(is);
        }
    }

}
