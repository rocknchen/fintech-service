package com.hthk.fintech.fintechservice.service.impl;

import com.hthk.common.model.Internet.message.email.MessageEmail;
import com.hthk.common.utils.FileUtils;
import com.hthk.fintech.config.ApplicationInfo;
import com.hthk.fintech.enumration.FTPTypeEnum;
import com.hthk.fintech.exception.InvalidRequestException;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.fintechservice.service.FTPSyncService;
import com.hthk.fintech.fintechservice.service.basic.AbstractFTPService;
import com.hthk.fintech.model.net.ftp.FTPConnection;
import com.hthk.fintech.model.net.ftp.FTPSource;
import com.hthk.fintech.model.net.ftp.FTPSourceFolder;
import com.hthk.fintech.model.net.network.EmailInfo;
import com.hthk.fintech.model.net.network.SyncInfo;
import com.hthk.fintech.service.FTPClientService;
import com.hthk.fintech.structure.utils.JacksonUtils;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
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

    @Override
    public void start(boolean loop, int sleepSec) throws InvalidRequestException, ServiceInternalException, InterruptedException {

        logger.info(LOG_DEFAULT, "FTPSyncService", "start");

        Map<String, FTPSourceFolder> ftpSourceMap = buildFTPSourceMap(appInfo);
        Set<String> ftpSourceIdSet = buildSourceId(ftpSourceMap);
        logger.info(LOG_WRAP, "ftpSourceIdSet", ftpSourceIdSet);

        List<SyncInfo> ftpSyncList = appInfo.getFtpSyncList();
        Map<String, List<String>> emailMap = buildEmailMap(appInfo);

        start(loop, sleepSec, ftpSyncList, ftpSourceMap, ftpSourceIdSet, emailMap);

    }

    private void process(SyncInfo syncInfo, Map<String, FTPSourceFolder> ftpSourceMap, Map<String, FTPConnection> connectionMap, Map<String, List<String>> emailMap) throws InvalidRequestException, ServiceInternalException, IOException, JSchException, SftpException {

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
                    List<String> recList = buildList(syncInfo.getEmailReceiveList(), emailMap);
                    List<String> ccList = buildList(syncInfo.getEmailCCList(), emailMap);
                    MessageEmail msg = buildMsg(name, tmpFolder, syncInfo.getSubject(), recList, ccList);
                    emailService.send(msg);
                }
                new File(fileInTmp).delete();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    private List<String> buildList(List<String> emailReceiveList, Map<String, List<String>> emailMap) {
        List<String> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(emailReceiveList)) {
            return Arrays.asList();
        }
        emailReceiveList.forEach(t -> {
            List<String> subList = emailMap.get(t);
            list.addAll(subList);
        });
        return list;
    }

    private Map<String, List<String>> buildEmailMap(ApplicationInfo appInfo) {

        List<EmailInfo> emailList = appInfo.getEmailList();
        Map<String, String> origMap = emailList.stream().collect(Collectors.toMap(EmailInfo::getName, EmailInfo::getList));
        Map<String, List<String>> emailMap = new HashMap<>();
        origMap.forEach((k, v) -> emailMap.put(k, Arrays.stream(v.split(",")).map(t -> t.trim()).collect(Collectors.toList())));
        return emailMap;
    }

    private MessageEmail buildMsg(String name, String tmpFolder, String subject, List<String> recList, List<String> ccList) {
        MessageEmail email = new MessageEmail();
        email.setTitle(subject);
        String srcFile = tmpFolder + "/" + name;
        String content = FileUtils.readResourceAsStr(new File(srcFile), true);
        email.setContent(content);
        email.setReceiverList(recList);
        email.setCcList(ccList);
        email.setAttachmentList(Arrays.asList(srcFile));
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

    private void start(boolean loop, int sleepSec, List<SyncInfo> ftpSyncList, Map<String, FTPSourceFolder> ftpSourceMap, Set<String> ftpSourceIdSet, Map<String, List<String>> emailMap) throws InterruptedException {

        if (loop) {
            while (true) {

                try {
                    Map<String, FTPConnection> connectionMap = build(appInfo, ftpSourceIdSet);
                    logger.info(LOG_WRAP, "connection done", JacksonUtils.toYMLPrettyTry(connectionMap.keySet()));
                    logger.info(LOG_WRAP, "ftpSyncList: {}", Optional.ofNullable(ftpSyncList).map(t -> JacksonUtils.toYMLPrettyTry(t)).orElse(null));
                    process(ftpSyncList, ftpSourceMap, connectionMap, emailMap);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                Thread.sleep(1000 * sleepSec);
            }
        } else {

            Map<String, FTPConnection> connectionMap = build(appInfo, ftpSourceIdSet);
            logger.info(LOG_WRAP, "connection done", JacksonUtils.toYMLPrettyTry(connectionMap.keySet()));

            process(ftpSyncList, ftpSourceMap, connectionMap, emailMap);
        }
    }

    private void process(List<SyncInfo> ftpSyncList, Map<String, FTPSourceFolder> ftpSourceMap, Map<String, FTPConnection> connectionMap, Map<String, List<String>> emailMap) {
        ftpSyncList.stream().forEach(t -> {
            try {
                process(t, ftpSourceMap, connectionMap, emailMap);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        after(ftpSyncList, ftpSourceMap, connectionMap);
    }

    private void after(List<SyncInfo> ftpSyncList, Map<String, FTPSourceFolder> ftpSourceMap, Map<String, FTPConnection> connectionMap) {

        Set<String> disconnectSet = new HashSet<>();
        ftpSyncList.stream().forEach(t -> {
            try {
                String sourceId = t.getSource();
                String destId = t.getDest();
                FTPSourceFolder sourceFolderInfo = ftpSourceMap.get(sourceId);
                FTPSourceFolder destFolderInfo = ftpSourceMap.get(destId);
                if (!disconnectSet.contains(sourceId)) {
                    after(sourceFolderInfo, connectionMap);
                    disconnectSet.add(sourceId);
                }
                if (!disconnectSet.contains(destId)) {
                    after(destFolderInfo, connectionMap);
                    disconnectSet.add(destId);
                }
            } catch (Exception e) {
//                throw new RuntimeException(e);
            }
        });
    }

    private List<String> listFolder(FTPConnection connection, String changeFolder) throws InvalidRequestException, ServiceInternalException, IOException {
        FTPClientService clientService = getService(connection.getType());
        return clientService.list(connection, changeFolder);
    }

}
