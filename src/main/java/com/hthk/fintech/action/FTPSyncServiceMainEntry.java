package com.hthk.fintech.action;

import com.hthk.fintech.exception.InvalidRequestException;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.fintechservice.service.FTPSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static com.hthk.fintech.config.FintechStaticData.DEFAULT_APP_CONTEXT_FILE;

/**
 * @Author: Rock CHEN
 * @Date: 2024/3/21 11:09
 */
public class FTPSyncServiceMainEntry {

    private final static Logger logger = LoggerFactory.getLogger(FileMigrateMainEntry.class);

    private void process() throws InterruptedException, InvalidRequestException, ServiceInternalException {

        boolean isLoop = true;
        int sleepSec = 60 * 2;

        ApplicationContext appContext = new ClassPathXmlApplicationContext(DEFAULT_APP_CONTEXT_FILE);
        FTPSyncService ftpSyncService = appContext.getBean(FTPSyncService.class);
        ftpSyncService.start(isLoop, sleepSec);
    }

    public static void main(String[] args) throws InterruptedException, InvalidRequestException, ServiceInternalException {
        new FTPSyncServiceMainEntry().process();
    }

}
