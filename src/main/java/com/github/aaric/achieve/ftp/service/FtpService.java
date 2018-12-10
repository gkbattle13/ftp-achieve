package com.github.aaric.achieve.ftp.service;

import java.io.File;
import java.util.Map;

/**
 * FTP文件服务Service
 *
 * @author Aaric, created on 2018-12-10T21:16.
 * @since 0.1.0-SNAPSHOT
 */
public interface FtpService {

    /**
     * 检查是否已经存在该路径文件
     *
     * @param remotePath 远程FTP文件相对路径，例如“/uploads/test/test.txt”
     * @return 默认返回true，文件已经存在
     */
    boolean isHasFile(String remotePath);

    /**
     * 上传文件到FTP工作目录
     *
     * @param remotePath 上传到FTP相对路径，例如“/uploads/test/test.txt”，其中“/uploads”必须已经存在，否则因权限问题失败
     * @param uploadFile 需要上传到FTP的本地文件
     * @return 默认返回false，上传文件失败
     */
    boolean uploadFile(String remotePath, File uploadFile);

    /**
     * 批量上传文件到FTP工作目录
     *
     * @param mapUploadFiles 需要上传到FTP的本地文件，键名为指定FTP相对路径，键值为上传文件
     */
    void uploadFiles(Map<String, File> mapUploadFiles);

    /**
     * 下载文件到本地
     *
     * @param remoteFilePath     远程文件存储路径，例如/upload/test/test.txt
     * @param localFileDirectory 本地文件存储目录，例如：D:\download\file。若此值为null,则为临时文件存储目录
     * @param localFileName      本地存储文件名称，例如test.txt。 若此值为null,则为远程文件名称
     * @return
     */
    File downloadFile(String remoteFilePath, String localFileDirectory, String localFileName);

}
