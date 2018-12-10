package com.github.aaric.achieve.ftp.service;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

/**
 * FtpServiceTest
 *
 * @author Aaric, created on 2018-12-10T21:30.
 * @since 0.1.0-SNAPSHOT
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class FtpServiceTest {

    /**
     * 测试文件
     */
    private static final String testFileDirectory = "C:\\Users\\root\\Desktop\\";
    private static final String testFileName = "OpenBSD.jpg";

    @Autowired
    private FtpService ftpService;

    @Test
    @Ignore
    public void testUploadFile() {
        ftpService.uploadFile("/" + testFileName, new File(testFileDirectory, testFileName));
    }
}
