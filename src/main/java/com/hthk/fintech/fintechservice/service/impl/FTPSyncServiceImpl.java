package com.hthk.fintech.fintechservice.service.impl;

import com.hthk.fintech.config.ApplicationInfo;
import com.hthk.fintech.exception.InvalidRequestException;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.fintechservice.service.FTPSyncService;
import com.hthk.fintech.model.net.ftp.FTPConnection;
import com.hthk.fintech.model.net.ftp.FTPSource;
import com.hthk.fintech.service.FTPClientService;
import com.hthk.fintech.structure.utils.JacksonUtils;
import com.jcraft.jsch.JSchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hthk.fintech.config.FintechStaticData.*;

/**
 * @Author: Rock CHEN
 * @Date: 2024/3/21 13:29
 */
@Component
public class FTPSyncServiceImpl implements FTPSyncService {

    private final static Logger logger = LoggerFactory.getLogger(FTPSyncServiceImpl.class);

    @Autowired
    private ApplicationInfo appInfo;

    @Qualifier
    @Resource(name = "ftpService")
    private FTPClientService ftpClientService;

    @Qualifier
    @Resource(name = "sftpService")
    private FTPClientService sftpClientService;

    @Override
    public void start() {

        logger.info(LOG_DEFAULT, "FTPSyncService", "start");

        Map<String, FTPConnection> connectionMap = build(appInfo);
        logger.info(LOG_WRAP, "connection done", JacksonUtils.toYMLPrettyTry(connectionMap.keySet()));
//        List<FTPSyncJob> jobList = buildJobList();
    }

    private Map<String, FTPConnection> build(ApplicationInfo appInfo) {

        List<FTPSource> ftpSourceList = appInfo.getFtpSourceList();
        List<FTPConnection> connectionList = ftpSourceList.stream().map(t -> {
            try {
                return connect(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        return connectionList.stream().collect(Collectors.toMap(FTPConnection::getId, Function.identity()));
    }

    private FTPClientService getService(FTPSource ftpSource) throws InvalidRequestException {
        switch (ftpSource.getType()) {
            case FTP:
                return ftpClientService;
            case SFTP:
                return sftpClientService;
            default:
                throw new InvalidRequestException(KW_NOT_SUPPORTED);
        }
    }

    private FTPConnection connect(FTPSource ftpSource) throws IOException, InvalidRequestException, ServiceInternalException, JSchException {
        FTPConnection conn = new FTPConnection();
        BeanUtils.copyProperties(ftpSource, conn);
        FTPClientService clientService = getService(ftpSource);
        logger.info(LOG_DEFAULT, "connect", ftpSource.getId());
        return clientService.connect(ftpSource);
    }

}
