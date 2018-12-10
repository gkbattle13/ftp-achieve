package com.github.aaric.achieve.ftp.controller;

import com.github.aaric.achieve.ftp.controller.api.ResourceApi;
import com.github.aaric.achieve.ftp.service.FtpService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 资源模块控制器
 *
 * @author Aaric, created on 2018-12-10T21:47.
 * @since 0.2.0-SNAPSHOT
 */
@RestController
@RequestMapping("/api/resource")
public class ResourceController implements ResourceApi {

    @Autowired
    private FtpService ftpService;

    @Override
    @RequestMapping(value = "/uploadImage", method = RequestMethod.POST)
    public String uploadImage(@RequestPart("uploadFile") MultipartFile uploadFile) throws IOException {
        // 校验文件信息
        if (null != uploadFile && !uploadFile.isEmpty()) {
            // 源文件名
            String originalFilename = uploadFile.getOriginalFilename();
            // 新文件名：UUID+文件后缀
            String storageFileName = UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf("."));
            File storageFile = new File(FileUtils.getTempDirectory(), storageFileName);
            // 存储文件
            uploadFile.transferTo(storageFile);
            // 上传到FTP服务器
            String ftpRelativePath = "/" + storageFileName;
            ftpService.uploadFile(ftpRelativePath, storageFile);
            // 返回文件信息
            return "/files" + ftpRelativePath;
        }
        return null;
    }
}
