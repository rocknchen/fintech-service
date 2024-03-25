package com.hthk.fintech.fintechservice.service.impl;

import com.hthk.common.internet.email.service.EmailService;
import com.hthk.common.model.Internet.message.email.MessageEmail;
import com.hthk.fintech.config.AppConfig;
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
import com.jcraft.jsch.SftpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
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

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private EmailService emailService;

    @Override
    public void start(boolean loop, int sleepSec) throws InvalidRequestException, ServiceInternalException, InterruptedException {

        logger.info(LOG_DEFAULT, "FTPSyncService", "start");

        Map<String, FTPSourceFolder> ftpSourceMap = buildFTPSourceMap(appInfo);

        List<SyncInfo> ftpSyncList = appInfo.getFtpSyncList();

        start(loop, sleepSec, ftpSyncList, ftpSourceMap);

    }

    private void process(SyncInfo syncInfo, Map<String, FTPSourceFolder> ftpSourceMap, Map<String, FTPConnection> connectionMap) throws InvalidRequestException, ServiceInternalException, IOException, JSchException, SftpException {

        String sourceId = syncInfo.getSource();
        String destId = syncInfo.getDest();

        FTPSourceFolder sourceFolderInfo = ftpSourceMap.get(sourceId);
        FTPSourceFolder destFolderInfo = ftpSourceMap.get(destId);

        before(sourceFolderInfo, connectionMap);
        before(destFolderInfo, connectionMap);

        List<String> srcFileNameList = getFileNameList(sourceFolderInfo, connectionMap);
        logger.info("{}: {} {} {}\r\n{}", "source list", sourceId, sourceFolderInfo.getFolder(), Optional.ofNullable(srcFileNameList).map(List::size).orElse(0), srcFileNameList);

        List<String> destFileNameList = getFileNameList(destFolderInfo, connectionMap);
        logger.info("{}: {} {} {}\r\n{}", "dest list", destId, destFolderInfo.getFolder(), Optional.ofNullable(destFileNameList).map(List::size).orElse(0), destFileNameList);

        List<String> newFileNameList = srcFileNameList.stream().filter(name -> !destFileNameList.contains(name)).collect(Collectors.toList());
        logger.info("{}\r\n{} {}", "copy(new) list", Optional.ofNullable(newFileNameList).map(List::size).orElse(0), newFileNameList);

        if (CollectionUtils.isEmpty(newFileNameList)) {
            return;
        }

        String tmpFolder = appConfig.getTmpFolder();

        newFileNameList.forEach(name -> {
            logger.info("send {}", name);
            try {
                String fileInTmp = download(name, tmpFolder, sourceFolderInfo, connectionMap);
                upload(fileInTmp, destFolderInfo, connectionMap);
                if (syncInfo.isBackup()) {
                    move(name, syncInfo, sourceFolderInfo, connectionMap);
                }
                if (syncInfo.getSendEmail() != null && syncInfo.getSendEmail() == true) {
                    MessageEmail msg = buildMsg(name, tmpFolder, syncInfo.getSubject());
                    emailService.send(msg);
                }
                new File(fileInTmp).delete();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        after(sourceFolderInfo, connectionMap);
        after(destFolderInfo, connectionMap);
    }

    private MessageEmail buildMsg(String name, String tmpFolder, String subject) {
        MessageEmail email = new MessageEmail();
        email.setTitle(subject);
        email.setReceiverList(Arrays.asList("rockchen@htsc.com"));
        email.setAttachmentList(Arrays.asList(tmpFolder + "/" + name));
        email.setSign("SCB Test User");
        return email;
    }

    private void move(String name, SyncInfo syncInfo, FTPSourceFolder folderInfo, Map<String, FTPConnection> connectionMap) throws InvalidRequestException, IOException {

        FTPConnection connection = getConnect(folderInfo, connectionMap);
        FTPClientService clientService = getService(connection.getType());
        clientService.move(connection, folderInfo.getFolder() + "/" + name, syncInfo.getBackup());
    }

    private void after(FTPSourceFolder folderInfo, Map<String, FTPConnection> connectionMap) throws InvalidRequestException, IOException {

        FTPConnection connection = getConnect(folderInfo, connectionMap);
        FTPClientService clientService = getService(connection.getType());
        clientService.after(connection);
    }

    private void before(FTPSourceFolder folderInfo, Map<String, FTPConnection> connectionMap) throws InvalidRequestException, JSchException, SftpException {

        FTPConnection connection = getConnect(folderInfo, connectionMap);
        FTPClientService clientService = getService(connection.getType());
        clientService.before(connection);
    }

    private void upload(String fileInTmp, FTPSourceFolder folderInfo, Map<String, FTPConnection> connectionMap) throws InvalidRequestException, IOException, ServiceInternalException {

        String folder = folderInfo.getFolder();
        FTPConnection connection = getConnect(folderInfo, connectionMap);
        FTPClientService clientService = getService(connection.getType());
        clientService.upload(connection, folder, fileInTmp);
    }

    private String download(String name, String tmpFolder, FTPSourceFolder folderInfo, Map<String, FTPConnection> connectionMap) throws InvalidRequestException, IOException, ServiceInternalException {

        String folder = folderInfo.getFolder();
        FTPConnection connection = getConnect(folderInfo, connectionMap);
        FTPClientService clientService = getService(connection.getType());
        return clientService.download(connection, folder, name, tmpFolder);
    }

    private List<String> getFileNameList(FTPSourceFolder folderInfo, Map<String, FTPConnection> connectionMap) throws InvalidRequestException, ServiceInternalException, IOException {

        String folder = folderInfo.getFolder();
        FTPConnection connection = getConnect(folderInfo, connectionMap);
        return listFolder(connection, folder);
    }

    private FTPConnection getConnect(FTPSourceFolder folderInfo, Map<String, FTPConnection> connectionMap) {
        String sourceId = folderInfo.getSourceId();
        return connectionMap.get(sourceId);
    }

    private void start(boolean loop, int sleepSec, List<SyncInfo> ftpSyncList, Map<String, FTPSourceFolder> ftpSourceMap) throws InterruptedException {

        if (loop) {
            while (true) {

                try {
                    Map<String, FTPConnection> connectionMap = build(appInfo);
                    logger.info(LOG_WRAP, "connection done", JacksonUtils.toYMLPrettyTry(connectionMap.keySet()));
                    process(ftpSyncList, ftpSourceMap, connectionMap);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                Thread.sleep(1000 * sleepSec);
            }
        } else {

            Map<String, FTPConnection> connectionMap = build(appInfo);
            logger.info(LOG_WRAP, "connection done", JacksonUtils.toYMLPrettyTry(connectionMap.keySet()));

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
