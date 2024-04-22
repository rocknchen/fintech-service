package com.hthk.fintech.fintechservice.service.impl;

import com.hthk.fintech.config.ApplicationInfo;
import com.hthk.fintech.exception.InvalidRequestException;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.fintechservice.service.FileMonitorService;
import com.hthk.fintech.fintechservice.service.basic.AbstractFTPService;
import com.hthk.fintech.model.file.MonitorCompleteInfo;
import com.hthk.fintech.model.file.MonitorInfo;
import com.hthk.fintech.model.net.ftp.FTPSourceFile;
import com.hthk.fintech.model.net.network.EmailInfo;
import com.hthk.fintech.model.task.QuartzScheduledJob;
import com.hthk.fintech.structure.utils.JacksonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
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
    public void start(Scheduler scheduler) throws InvalidRequestException, ServiceInternalException, InterruptedException {

        logger.info(LOG_DEFAULT, "FileMonitorService", "start");
        Map<String, FTPSourceFile> ftpSourceFileMap = buildFTPSourceFileMap(appInfo);
        logger.info(LOG_DEFAULT, "ftpSourceFileMap", JacksonUtils.toYMLPrettyTry(ftpSourceFileMap));

        List<MonitorCompleteInfo> mciList = buildMCIList(ftpSourceFileMap);
        logger.info(LOG_DEFAULT, "mciList", JacksonUtils.toYMLPrettyTry(mciList));

        Set<String> ftpSourceIdSet = buildSourceIdForFile(ftpSourceFileMap);
        logger.info(LOG_WRAP, "ftpSourceIdSet", ftpSourceIdSet);

        Map<String, List<String>> emailMap = buildEmailMap(appInfo);

        start(scheduler, mciList, ftpSourceIdSet, emailMap);
    }

    private Map<String, List<String>> buildEmailMap(ApplicationInfo appInfo) {
        List<EmailInfo> emailList = appInfo.getEmailList();
        Map<String, String> origMap = emailList.stream().collect(Collectors.toMap(EmailInfo::getName, EmailInfo::getList));
        Map<String, List<String>> emailMap = new HashMap<>();
        origMap.forEach((k, v) -> emailMap.put(k, Arrays.stream(v.split(",")).map(t -> t.trim()).collect(Collectors.toList())));
        return emailMap;
    }

    private void start(Scheduler scheduler, QuartzScheduledJob job, Set<String> ftpSourceIdSet) throws SchedulerException {

        scheduler.scheduleJob(job.getJobDetail(), job.getTrigger());
        scheduler.start();
    }

    private void start(Scheduler scheduler, List<MonitorCompleteInfo> mciList, Set<String> ftpSourceIdSet, Map<String, List<String>> emailMap) {

        List<QuartzScheduledJob> jobList = buildJobList(mciList, emailMap);
        logger.info("jobList: {}", jobList.size());
        jobList.stream().forEach(job -> {
            try {
                start(scheduler, job, ftpSourceIdSet);
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private QuartzScheduledJob buildJob(MonitorCompleteInfo mci, Map<String, List<String>> emailMap) {

        String group = "default";
        String name = mci.getMonitorInfo().getId();
        String expression = mci.getMonitorInfo().getTriggerExpression();
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(name, group).startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(expression))
                .build();

        String jobGroup = group;
        String jobName = "job_" + name;

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("appInfo", appInfo);
        jobDataMap.put("mci", mci);
        jobDataMap.put("FTP_CLIENT", ftpClientService);
        jobDataMap.put("SFTP_CLIENT", sftpClientService);
        jobDataMap.put("EMAIL_MAP", emailMap);
        jobDataMap.put("EMAIL_SERVICE", emailService);

        JobDetail jobDetail = JobBuilder.newJob(QuartzJobWrapper.class)
                .usingJobData(jobDataMap)
                .withIdentity(jobName, jobGroup).build();

        return new QuartzScheduledJob(trigger, jobDetail);
    }

    private List<QuartzScheduledJob> buildJobList(List<MonitorCompleteInfo> mciList, Map<String, List<String>> emailMap) {
        return mciList.stream().map(t -> buildJob(t, emailMap)).collect(Collectors.toList());
    }

    private MonitorCompleteInfo buildMCI(MonitorInfo mi, Map<String, FTPSourceFile> ftpSourceFileMap) {
        String refId = mi.getReferenceId();
        FTPSourceFile ftpSourceFile = ftpSourceFileMap.get(refId);
        return ftpSourceFile == null ? null : new MonitorCompleteInfo(mi, ftpSourceFile);
    }

    private List<MonitorCompleteInfo> buildMCIList(Map<String, FTPSourceFile> ftpSourceFileMap) {

        List<MonitorInfo> monitorInfoList = getMonitorInfoList(appInfo);
        logger.info(LOG_WRAP, "monitorInfoList", CollectionUtils.isEmpty(monitorInfoList) ? null : JacksonUtils.toYMLPrettyTry(monitorInfoList));

        return monitorInfoList.stream().map(t -> buildMCI(t, ftpSourceFileMap)).filter(t -> t != null).collect(Collectors.toList());
    }

    private List<MonitorInfo> getMonitorInfoList(ApplicationInfo appInfo) {
        return appInfo.getMonitorInfoList();
    }

}
