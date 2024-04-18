package com.hthk.fintech.fintechservice.service.basic;

import com.hthk.common.internet.email.service.EmailService;
import com.hthk.fintech.config.AppConfig;
import com.hthk.fintech.config.ApplicationInfo;
import com.hthk.fintech.model.net.ftp.FTPSourceFile;
import com.hthk.fintech.model.net.ftp.FTPSourceFolder;
import com.hthk.fintech.service.FTPClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

}
