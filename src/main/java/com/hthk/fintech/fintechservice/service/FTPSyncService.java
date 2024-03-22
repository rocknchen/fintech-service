package com.hthk.fintech.fintechservice.service;

import com.hthk.fintech.exception.InvalidRequestException;
import com.hthk.fintech.exception.ServiceInternalException;

/**
 * @Author: Rock CHEN
 * @Date: 2024/3/21 12:05
 */
public interface FTPSyncService {

    void start(boolean loop, int sleepSec) throws InvalidRequestException, ServiceInternalException, InterruptedException;
}
