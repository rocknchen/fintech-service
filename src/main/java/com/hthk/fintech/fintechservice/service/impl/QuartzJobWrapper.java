package com.hthk.fintech.fintechservice.service.impl;

import com.hthk.common.utils.FileUtils;
import com.hthk.fintech.config.ApplicationInfo;
import com.hthk.fintech.enumration.FTPTypeEnum;
import com.hthk.fintech.exception.InvalidRequestException;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.model.file.MonitorCompleteInfo;
import com.hthk.fintech.model.net.ftp.FTPConnection;
import com.hthk.fintech.model.net.ftp.FTPSource;
import com.hthk.fintech.service.FTPClientService;
import com.jcraft.jsch.JSchException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.hthk.fintech.config.FintechStaticData.KW_NOT_SUPPORTED;
import static com.hthk.fintech.config.FintechStaticData.LOG_DEFAULT;

/**
 * @Author: Rock CHEN
 * @Date: 2024/4/22 11:23
 */
public class QuartzJobWrapper implements Job {

    private final Logger logger = LoggerFactory.getLogger(QuartzJobWrapper.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        logger.info("execute");

        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        ApplicationInfo appInfo = (ApplicationInfo) jobDataMap.get("appInfo");
        MonitorCompleteInfo mci = (MonitorCompleteInfo) jobDataMap.get("mci");
        String sourceId = mci.getFtpFile().getSourceId();

        List<FTPSource> ftpSourceList = appInfo.getFtpSourceList();
        FTPSource ftpSource = getFTPSource(sourceId, ftpSourceList);

        FTPClientService ftpClientService = (FTPClientService) jobDataMap.get("FTP_CLIENT");
        FTPClientService sftpClientService = (FTPClientService) jobDataMap.get("SFTP_CLIENT");

        try {
            FTPConnection ftpConnection = build(appInfo, ftpSource, ftpClientService, sftpClientService);
        } catch (Exception e) {
            throw new JobExecutionException(e.getMessage(), e);
        }

        FileUtils.build("C:/Rock/Datas/IT/DEV_Datas/tmp/test.txt", "test", true);
    }

    private FTPSource getFTPSource(String sourceId, List<FTPSource> ftpSourceList) {
        return ftpSourceList.stream().filter(t -> t.getId().equals(sourceId)).collect(Collectors.toList()).get(0);
    }

    private FTPConnection build(ApplicationInfo appInfo, FTPSource ftpSource, FTPClientService ftpClientService, FTPClientService sftpClientService) throws JSchException, InvalidRequestException, IOException, ServiceInternalException {

        List<FTPSource> ftpSourceList = appInfo.getFtpSourceList();
        return connect(ftpSource, ftpClientService, sftpClientService);
    }

    protected FTPConnection connect(FTPSource ftpSource, FTPClientService ftpClientService, FTPClientService sftpClientService) throws IOException, InvalidRequestException, ServiceInternalException, JSchException {
        FTPConnection conn = new FTPConnection();
        BeanUtils.copyProperties(ftpSource, conn);
        FTPClientService clientService = getService(ftpSource.getType(), ftpClientService, sftpClientService);
        logger.info(LOG_DEFAULT, "connect", ftpSource.getId());
        return clientService.connect(ftpSource);
    }

    protected FTPClientService getService(FTPTypeEnum ftpType, FTPClientService ftpClientService, FTPClientService sftpClientService) throws InvalidRequestException {
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
