package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.trade.criteria.CriteriaTrade;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.fintechservice.config.AppConfig;
import com.hthk.fintech.model.data.datacenter.query.EntityTypeEnum;
import com.hthk.fintech.model.software.app.ApplicationEnum;
import com.hthk.fintech.model.software.app.ApplicationInstance;
import com.hthk.fintech.model.web.http.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/5 16:47
 */
public class RemoteTradeServiceTest {

    RemoteTradeService remoteTradeService = new RemoteTradeService();

    public RemoteTradeServiceTest() {
        AppConfig appConfig = new AppConfig();
        appConfig.setServiceName("services");
        appConfig.setServiceUrl("http://127.0.0.1");
        appConfig.setInstanceList(Arrays.asList("87v17;30087", "129v17;30129"));
        remoteTradeService.setFsAppConfig(appConfig);
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetTrade_BY_TRADEFILTER() throws ServiceInternalException {

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        instance.setInstance("87v17");

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        CriteriaTrade criteria = new CriteriaTrade();
        criteria.setBookList(Arrays.asList("CIFXDH"));
        criteria.setTradeFilter("HTHK_FICC_TEST");

        remoteTradeService.getTrade(instance, dateTime, criteria);
    }

}