package com.hthk.fintech.fintechservice.service.basic;

import com.hthk.common.internet.email.service.EmailService;
import com.hthk.fintech.config.AppConfig;
import com.hthk.fintech.config.ApplicationInfo;
import com.hthk.fintech.service.FTPClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.Resource;

/**
 * @Author: Rock CHEN
 * @Date: 2024/3/21 20:59
 */
public abstract class AbstractFTPService {

    @Qualifier
    @Resource(name = "ftpService")
    protected FTPClientService ftpClientService;

    @Qualifier
    @Resource(name = "sftpService")
    protected FTPClientService sftpClientService;

    @Autowired
    protected ApplicationInfo appInfo;

    @Autowired
    protected AppConfig appConfig;

    @Autowired
    protected EmailService emailService;

}
