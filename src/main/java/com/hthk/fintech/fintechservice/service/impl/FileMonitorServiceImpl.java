package com.hthk.fintech.fintechservice.service.impl;

import com.hthk.fintech.config.ApplicationInfo;
import com.hthk.fintech.exception.InvalidRequestException;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.fintechservice.service.FileMonitorService;
import com.hthk.fintech.fintechservice.service.basic.AbstractFTPService;
import com.hthk.fintech.model.file.MonitorCompleteInfo;
import com.hthk.fintech.model.file.MonitorInfo;
import com.hthk.fintech.model.net.ftp.FTPSourceFile;
import com.hthk.fintech.structure.utils.JacksonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hthk.fintech.config.FintechStaticData.LOG_DEFAULT;
import static com.hthk.fintech.config.FintechStaticData.LOG_WRAP;

/**
 * @Author: Rock CHEN
 * @Date: 2024/4/18 15:18
 */
@Component
public class FileMonitorServiceImpl

        extends AbstractFTPService

        implements FileMonitorService {

    private final static Logger logger = LoggerFactory.getLogger(FileMonitorServiceImpl.class);

    @Override
    public void start() throws InvalidRequestException, ServiceInternalException, InterruptedException {

        logger.info(LOG_DEFAULT, "FileMonitorService", "start");
        List<MonitorCompleteInfo> mciList = buildMCIList();
        logger.info(LOG_DEFAULT, "mciList", JacksonUtils.toYMLPrettyTry(mciList));

        Map<String, FTPSourceFile> ftpSourceFileMap = buildFTPSourceFileMap(appInfo);
        Set<String> ftpSourceIdSet = buildSourceIdForFile(ftpSourceFileMap);
        logger.info(LOG_WRAP, "ftpSourceIdSet", ftpSourceIdSet);

    }

    private MonitorCompleteInfo buildMCI(MonitorInfo mi, Map<String, FTPSourceFile> ftpSourceFileMap) {
        String refId = mi.getReferenceId();
        FTPSourceFile ftpSourceFile = ftpSourceFileMap.get(refId);
        return ftpSourceFile == null ? null : new MonitorCompleteInfo(mi, ftpSourceFile);
    }

    private List<MonitorCompleteInfo> buildMCIList() {

        List<MonitorInfo> monitorInfoList = getMonitorInfoList(appInfo);
        logger.info(LOG_WRAP, "monitorInfoList", CollectionUtils.isEmpty(monitorInfoList) ? null : JacksonUtils.toYMLPrettyTry(monitorInfoList));

        Map<String, FTPSourceFile> ftpSourceFileMap = buildFTPSourceFileMap(appInfo);
        logger.info(LOG_DEFAULT, "ftpSourceFileMap", JacksonUtils.toYMLPrettyTry(ftpSourceFileMap));

        return monitorInfoList.stream().map(t -> buildMCI(t, ftpSourceFileMap)).filter(t -> t != null).collect(Collectors.toList());
    }

    private List<MonitorInfo> getMonitorInfoList(ApplicationInfo appInfo) {
        return appInfo.getMonitorInfoList();
    }

}
