package com.hthk.fintech.fintechservice.service.basic;

import com.hthk.common.internet.email.service.EmailService;
import com.hthk.fintech.config.AppConfig;
import com.hthk.fintech.config.ApplicationInfo;
import com.hthk.fintech.enumration.FTPTypeEnum;
import com.hthk.fintech.exception.InvalidRequestException;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.fintechservice.service.impl.FTPSyncServiceImpl;
import com.hthk.fintech.model.net.ftp.FTPConnection;
import com.hthk.fintech.model.net.ftp.FTPSource;
import com.hthk.fintech.model.net.ftp.FTPSourceFile;
import com.hthk.fintech.model.net.ftp.FTPSourceFolder;
import com.hthk.fintech.service.FTPClientService;
import com.jcraft.jsch.JSchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hthk.fintech.config.FintechStaticData.KW_NOT_SUPPORTED;
import static com.hthk.fintech.config.FintechStaticData.LOG_DEFAULT;

/**
 * @Author: Rock CHEN
 * @Date: 2024/3/21 20:59
 */
public abstract class AbstractFTPService {

    private final static Logger logger = LoggerFactory.getLogger(AbstractFTPService.class);

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

    protected Map<String, FTPConnection> build(ApplicationInfo appInfo, Set<String> ftpSourceIdSet) {

        List<FTPSource> ftpSourceList = appInfo.getFtpSourceList();
        List<FTPConnection> connectionList = ftpSourceList.stream()
                .filter(t -> ftpSourceIdSet.contains(t.getId()))
                .map(t -> {
                    try {
                        return connect(t);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
        return connectionList.stream().collect(Collectors.toMap(FTPConnection::getId, Function.identity()));
    }

    protected FTPConnection connect(FTPSource ftpSource) throws IOException, InvalidRequestException, ServiceInternalException, JSchException {
        FTPConnection conn = new FTPConnection();
        BeanUtils.copyProperties(ftpSource, conn);
        FTPClientService clientService = getService(ftpSource.getType());
        logger.info(LOG_DEFAULT, "connect", ftpSource.getId());
        return clientService.connect(ftpSource);
    }

    protected Map<String, FTPSourceFolder> buildFTPSourceMap(ApplicationInfo appInfo) {
        List<FTPSourceFolder> ftpFolderList = appInfo.getRemoteSource().getFtpFolderList();
        return ftpFolderList.stream().collect(Collectors.toMap(FTPSourceFolder::getId, Function.identity()));
    }

    protected Map<String, FTPSourceFile> buildFTPSourceFileMap(ApplicationInfo appInfo) {
        List<FTPSourceFile> ftpList = appInfo.getRemoteSource().getFtpFileList();
        return ftpList.stream().collect(Collectors.toMap(FTPSourceFile::getId, Function.identity()));
    }

    protected Set<String> buildSourceId(Map<String, FTPSourceFolder> ftpSourceMap) {
        return ftpSourceMap.values().stream().map(t -> t.getSourceId()).collect(Collectors.toSet());
    }

    protected Set<String> buildSourceIdForFile(Map<String, FTPSourceFile> ftpSourceFileMap) {
        return ftpSourceFileMap.values().stream().map(t -> t.getSourceId()).collect(Collectors.toSet());
    }

    protected FTPClientService getService(FTPTypeEnum ftpType) throws InvalidRequestException {
        switch (ftpType) {
            case FTP:
                return ftpClientService;
            case SFTP:
                return sftpClientService;
            default:
                throw new InvalidRequestException(KW_NOT_SUPPORTED);
        }
    }

}
