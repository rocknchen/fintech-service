package com.hthk.fintech.fintechservice.service.impl;

import com.hthk.fintech.config.ApplicationInfo;
import com.hthk.fintech.enumration.FTPTypeEnum;
import com.hthk.fintech.exception.InvalidRequestException;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.fintechservice.service.FTPSyncService;
import com.hthk.fintech.fintechservice.service.basic.AbstractFTPService;
import com.hthk.fintech.model.net.ftp.FTPConnection;
import com.hthk.fintech.model.net.ftp.FTPSource;
import com.hthk.fintech.model.net.ftp.FTPSourceFolder;
import com.hthk.fintech.model.net.network.SyncInfo;
import com.hthk.fintech.service.FTPClientService;
import com.hthk.fintech.structure.utils.JacksonUtils;
import com.jcraft.jsch.JSchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
public class FTPSyncServiceImpl

        extends AbstractFTPService

        implements FTPSyncService {

    private final static Logger logger = LoggerFactory.getLogger(FTPSyncServiceImpl.class);

    @Autowired
    private ApplicationInfo appInfo;

    @Override
    public void start(boolean loop) throws InvalidRequestException, ServiceInternalException, InterruptedException {

        logger.info(LOG_DEFAULT, "FTPSyncService", "start");

        Map<String, FTPConnection> connectionMap = build(appInfo);
        logger.info(LOG_WRAP, "connection done", JacksonUtils.toYMLPrettyTry(connectionMap.keySet()));

        Map<String, FTPSourceFolder> ftpSourceMap = buildFTPSourceMap(appInfo);

        List<SyncInfo> ftpSyncList = appInfo.getFtpSyncList();

        start(loop, ftpSyncList, ftpSourceMap, connectionMap);

    }

    private void process(SyncInfo syncInfo, Map<String, FTPSourceFolder> ftpSourceMap, Map<String, FTPConnection> connectionMap) throws InvalidRequestException, ServiceInternalException, IOException {

        String sourceId = syncInfo.getSource();
        String destId = syncInfo.getDest();

        FTPSourceFolder sourceFolderInfo = ftpSourceMap.get(sourceId);
        FTPSourceFolder destFolderInfo = ftpSourceMap.get(destId);

        List<String> srcFileNameList = getFileNameList(sourceFolderInfo, connectionMap);
        logger.info(LOG_WRAP, "srcFileNameList", srcFileNameList);
    }

    private List<String> getFileNameList(FTPSourceFolder folderInfo, Map<String, FTPConnection> connectionMap) throws InvalidRequestException, ServiceInternalException, IOException {

        String sourceId = folderInfo.getSourceId();
        String folder = folderInfo.getFolder();
        FTPConnection connection = connectionMap.get(sourceId);
        return listFolder(connection, folder);
    }

    private void start(boolean loop, List<SyncInfo> ftpSyncList, Map<String, FTPSourceFolder> ftpSourceMap, Map<String, FTPConnection> connectionMap) throws InterruptedException {
        if (loop) {
            while (true) {
                process(ftpSyncList, ftpSourceMap, connectionMap);
                Thread.sleep(1000 * 10);
            }
        } else {
            process(ftpSyncList, ftpSourceMap, connectionMap);
        }
    }

    private void process(List<SyncInfo> ftpSyncList, Map<String, FTPSourceFolder> ftpSourceMap, Map<String, FTPConnection> connectionMap) {
        ftpSyncList.stream().forEach(t -> {
            try {
                process(t, ftpSourceMap, connectionMap);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private List<String> listFolder(FTPConnection connection, String changeFolder) throws InvalidRequestException, ServiceInternalException, IOException {
        FTPClientService clientService = getService(connection.getType());
        return clientService.list(connection, changeFolder);
    }

    private Map<String, FTPSourceFolder> buildFTPSourceMap(ApplicationInfo appInfo) {
        List<FTPSourceFolder> ftpFolderList = appInfo.getRemoteSource().getFtpFolderList();
        return ftpFolderList.stream().collect(Collectors.toMap(FTPSourceFolder::getId, Function.identity()));
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

    private FTPClientService getService(FTPTypeEnum ftpType) throws InvalidRequestException {
        switch (ftpType) {
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
        FTPClientService clientService = getService(ftpSource.getType());
        logger.info(LOG_DEFAULT, "connect", ftpSource.getId());
        return clientService.connect(ftpSource);
    }

}
