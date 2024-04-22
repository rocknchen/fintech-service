package com.hthk.fintech.fintechservice.service;

import com.hthk.fintech.exception.InvalidRequestException;
import com.hthk.fintech.exception.ServiceInternalException;
import org.quartz.Scheduler;

/**
 * @Author: Rock CHEN
 * @Date: 2024/4/18 15:17
 */
public interface FileMonitorService {

    void start(Scheduler scheduler) throws InvalidRequestException, ServiceInternalException, InterruptedException;

}
