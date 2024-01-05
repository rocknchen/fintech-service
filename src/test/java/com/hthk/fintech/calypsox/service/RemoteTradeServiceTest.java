package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.trade.criteria.CriteriaTrade;
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

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetTrade_BY_TRADEFILTER() {

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        CriteriaTrade criteria = new CriteriaTrade();
        criteria.setBookList(Arrays.asList("CIFXDH"));
        criteria.setTradeFilter("HTHK_FICC_TEST");

        remoteTradeService.getTrade(instance, dateTime, criteria);
    }

}