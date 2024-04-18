package com.hthk.fintech.action;

import com.hthk.fintech.exception.InvalidRequestException;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.fintechservice.service.FileMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static com.hthk.fintech.config.FintechStaticData.DEFAULT_APP_CONTEXT_FILE;

/**
 * @Author: Rock CHEN
 * @Date: 2024/4/18 15:16
 */
public class FileMonitorServiceMainEntry {

    private final static Logger logger = LoggerFactory.getLogger(FileMonitorServiceMainEntry.class);

    private void process() throws InterruptedException, InvalidRequestException, ServiceInternalException {

        ApplicationContext appContext = new ClassPathXmlApplicationContext(DEFAULT_APP_CONTEXT_FILE);
        FileMonitorService ftpSyncService = appContext.getBean(FileMonitorService.class);
        ftpSyncService.start();
    }

    public static void main(String[] args) throws InterruptedException, InvalidRequestException, ServiceInternalException {
        new FileMonitorServiceMainEntry().process();
    }

}
