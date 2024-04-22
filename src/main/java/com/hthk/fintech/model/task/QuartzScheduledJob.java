package com.hthk.fintech.model.task;


import org.quartz.CronTrigger;
import org.quartz.JobDetail;

/**
 * @Author: Rock CHEN
 * @Date: 2024/4/22 11:06
 */
public class QuartzScheduledJob {

    private CronTrigger trigger;

    private JobDetail jobDetail;

    public QuartzScheduledJob(CronTrigger trigger, JobDetail jobDetail) {
        this.trigger = trigger;
        this.jobDetail = jobDetail;
    }

    public CronTrigger getTrigger() {
        return trigger;
    }

    public JobDetail getJobDetail() {
        return jobDetail;
    }
}
