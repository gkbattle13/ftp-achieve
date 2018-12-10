package com.github.aaric.achieve.ftp.service.impl;

import com.github.aaric.achieve.ftp.service.FtpService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Iterator;
import java.util.Map;

/**
 * FTP文件服务Service实现
 *
 * @author Aaric, created on 2018-12-10T21:16.
 * @since 0.0.1-SNAPSHOT
 */
@Service
public class FtpServiceImpl implements FtpService {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(FtpServiceImpl.class);

    /**
     * UTF-8
     */
    private static final String HTTP_ENCODING_UTF_8 = "UTF-8";

    /**
     * ISO-8859-1
     */
    private static final String HTTP_ENCODING_ISO_8859_1 = "ISO-8859-1";

    /**
     * FTP端口
     */
    @Value("${ftp.port}")
    private int ftpPort;

    /**
     * FTP主机名称或者IP
     */
    @Value("${ftp.host}")
    private String ftpHost;

    /**
     * 访问FTP用户名
     */
    @Value("${ftp.username}")
    private String ftpUsername;

    /**
     * 访问FTP密码
     */
    @Value("${ftp.password}")
    private String ftpPassword;

    /**
     * FTP连接超时时间
     */
    @Value("${ftp.connectTimeout}")
    private int ftpConnectTimeout;

    /**
     * FTP数据传输超时时间
     */
    @Value("${ftp.dataTimeout}")
    private int ftpDataTimeout;

    @Override
    public boolean isHasFile(String remotePath) {
        // 定义是否成功切换工作目录变量
        boolean change = false;

        // 获得FTPClient对象
        FTPClient ftpClient = getFTPClient();
        if (null != ftpClient) {
            try {
                // 基本判断
                if (StringUtils.isNotBlank(remotePath)) {
                    // 获得查询工作目录名称
                    String queryPathName = remotePath.substring(0, remotePath.lastIndexOf("/") + 1);
                    System.out.println(queryPathName);

                    // 先判断目录是否存在，再判断文件是否存在
                    change = ftpClient.changeWorkingDirectory(getStringForIso(queryPathName));
                    if (!change) {
                        // 目录不存在，故文件不存在
                        return false;
                    } else {
                        // 获得查询文件名称
                        String queryFileName = remotePath.substring(remotePath.lastIndexOf("/") + 1);
                        // 遍历该目录的所有文件，检查是否存在文件
                        // flag: true->存在该文件 | false->不存在该文件
                        boolean flag = false;
                        for (FTPFile ftpFile : ftpClient.listFiles()) {
                            // 比较查询的文件名字和遍历查询得到的文件名称
                            System.out.println(ftpFile.getName());
                            if (StringUtils.equals(queryFileName, ftpFile.getName())) {
                                flag = true;
                                break;
                            }
                        }
                        return flag;
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    // 断开连接，释放连接资源
                    ftpClient.disconnect();
                    logger.info("关闭FTP连接...");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    @Override
    public boolean uploadFile(String remotePath, File uploadFile) {
        // 定义上传状态变量
        boolean flag = false;

        // 初始化FTPClient对象
        FTPClient ftpClient = getFTPClient();
        if (null != ftpClient) {
            try {
                // 上传文件
                flag = uploadFile(ftpClient, remotePath, uploadFile);

                // 断开连接，释放连接资源
                ftpClient.disconnect();
                logger.info("关闭FTP连接...");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    @Override
    public void uploadFiles(Map<String, File> mapUploadFiles) {
        // 初始化FTPClient对象
        FTPClient ftpClient = getFTPClient();
        if (null != ftpClient) {
            try {
                // 上传文件
                if (null != mapUploadFiles && 0 != mapUploadFiles.size()) {
                    Map.Entry<String, File> entry = null;
                    Iterator<Map.Entry<String, File>> it = mapUploadFiles.entrySet().iterator();
                    while (it.hasNext()) {
                        entry = it.next();
                        uploadFile(ftpClient, entry.getKey(), entry.getValue());
                    }
                }

                // 断开连接，释放连接资源
                ftpClient.disconnect();
                logger.info("关闭FTP连接...");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化与FTP建立连接
     *
     * @return
     * @throws IOException
     */
    private FTPClient getFTPClient() {
        try {
            // 初始化FTPClient
            FTPClient ftpClient = new FTPClient();

            // 设置通用参数
            ftpClient.connect(ftpHost, ftpPort);
            ftpClient.login(ftpUsername, ftpPassword);
            ftpClient.setControlEncoding(HTTP_ENCODING_UTF_8);
            ftpClient.setConnectTimeout(ftpConnectTimeout);
            ftpClient.setDataTimeout(ftpDataTimeout);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            // 如果连接失败则关闭连接
            if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                // 连接成功
                logger.info("连接FTP服务器成功...");
            } else {
                // 连接失败
                logger.info("连接FTP服务器失败...");
                ftpClient.disconnect();
            }

            return ftpClient;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获得ISO_8859_1编码字符串，解决FTP中文乱码问题
     *
     * @param string 字符串
     * @return
     * @throws UnsupportedEncodingException
     */
    private String getStringForIso(String string) throws UnsupportedEncodingException {
        return new String(string.getBytes(), HTTP_ENCODING_ISO_8859_1);
    }

    /**
     * 上传文件到FTP工作目录
     *
     * @param ftpClient  FTP对象
     * @param remotePath 上传到FTP相对路径，例如“/uploads/test/test.txt”，其中“/uploads”必须已经存在， 否则因权限问题失败
     * @param uploadFile 需要上传到FTP的本地文件
     * @return
     */
    private boolean uploadFile(FTPClient ftpClient, String remotePath, File uploadFile) {
        // 定义是否成功切换工作目录变量
        boolean change = false;
        // 一级一级目录名字
        String[] parents = null;

        try {
            // 切换并级联创建目录
            if (StringUtils.isNotBlank(remotePath)) {
                // 定义是否成功创建目录的变量
                boolean flag = false;
                // 获得一级一级目录名字
                parents = StringUtils.split(remotePath, "/");

                // 判断目录的层级，分别进行操作
                if (1 != parents.length) {
                    // 拼接目录信息
                    String join = "";
                    // 级联创建目录，忽略文件名字(length-1)
                    for (int i = 0; i < parents.length - 1; i++) {
                        // 拼接工作目录
                        join += "/" + parents[i];
                        // 切换到工作目录
                        change = ftpClient.changeWorkingDirectory(getStringForIso(join));
                        // 如果目录不存在，则创建该目录
                        if (!change) {
                            // 创建目录
                            flag = ftpClient.makeDirectory(getStringForIso(parents[i]));
                            if (flag) {
                                // 如果成功创建该目录，则第二次切换到该目录
                                change = ftpClient.changeWorkingDirectory(getStringForIso(join));
                            } else {
                                // 记录日志
                                logger.info("创建目录" + join + "失败...");
                            }
                        }
                    }

                } else {
                    // 直接切换到根目录("/")
                    change = ftpClient.changeWorkingDirectory("/");
                    logger.info("直接切换到根目录\"/\"...");

                }

            }

            // 切换到指定文件目录并上传文件到目录
            if (change) {
                // 定义是否成功上传文件的变量
                boolean upload = false;
                // 获得上传文件流对象
                InputStream input = FileUtils.openInputStream(uploadFile);
                if (null != input) {
                    // 远程文件名字为“parents[parents.length - 1]”字符串
                    upload = ftpClient.storeFile(getStringForIso(parents[parents.length - 1]), input);
                    // 关闭文件流
                    input.close();
                    // 记录日志
                    if (upload) {
                        logger.info("将\"" + uploadFile.getAbsolutePath() + "\"文件上传到FTP的\"" + remotePath + "\"目录成功...");
                    } else {
                        logger.info("将\"" + uploadFile.getAbsolutePath() + "\"文件上传到FTP的\"" + remotePath + "\"目录失败...");
                    }
                }

                return upload;
            }

        } catch (Exception e) {
            e.printStackTrace();

        }

        return false;
    }

    @Override
    public File downloadFile(String remoteFilePath, String localFileDirectory, String localFileName) {

        // 去掉 /files
        if (null != remoteFilePath && remoteFilePath.length() > 6) {
            String isFiles = remoteFilePath.substring(0, 6);
            if ("/files".equals(isFiles)) {
                remoteFilePath = remoteFilePath.substring(6, remoteFilePath.length());
            }
        }

        // 初始化FTPClient对象
        FTPClient ftpClient = getFTPClient();
        if (null != ftpClient) {
            OutputStream os = null;
            try {
                if (StringUtils.isNotBlank(remoteFilePath)) {
                    //远程文件目录
                    String remoteDirectory = remoteFilePath.substring(0, remoteFilePath.lastIndexOf("/") + 1);
                    //远程文件名称
                    String remoteFileName = remoteFilePath.substring(remoteFilePath.lastIndexOf("/") + 1);
                    //切换到指定目录
                    boolean changeDir = ftpClient.changeWorkingDirectory(getStringForIso(remoteDirectory));
                    if (changeDir) {
                        //遍历该目录的所有文件，检查是否存在文件
                        boolean fileIsExists = false;
                        for (FTPFile ftpFile : ftpClient.listFiles()) {
                            if (StringUtils.equals(remoteFileName, ftpFile.getName())) {
                                fileIsExists = true;
                                break;
                            }
                        }
                        if (fileIsExists) {
                            File dir = null;
                            if (null == localFileDirectory) {
                                localFileDirectory = FileUtils.getTempDirectoryPath();
                                dir = new File(localFileDirectory);
                            } else {
                                dir = new File(localFileDirectory);
                                if (!dir.exists()) {
                                    dir.mkdirs();
                                }
                            }
                            if (null == localFileName) {
                                localFileName = remoteFileName;
                            }
                            File file = new File(dir, localFileName);
                            os = new FileOutputStream(file);
                            if (ftpClient.retrieveFile(remoteFileName, os)) {
                                return file;
                            }
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (null != os) {
                        os.close();
                    }
                    ftpClient.disconnect();
                    logger.info("关闭FTP连接...");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}
