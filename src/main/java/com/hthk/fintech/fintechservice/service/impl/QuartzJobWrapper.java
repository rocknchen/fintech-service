package com.hthk.fintech.fintechservice.service.impl;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;

/**
 * @Author: Rock CHEN
 * @Date: 2024/4/22 11:23
 */
public class QuartzJobWrapper implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        Logger logger = (Logger) jobDataMap.get("logger");
        logger.info("execute");
    }

}
