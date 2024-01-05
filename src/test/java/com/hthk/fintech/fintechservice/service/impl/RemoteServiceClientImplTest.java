package com.hthk.fintech.fintechservice.service.impl;

import com.hthk.calypsox.model.trade.TradeInfoResultSet;
import com.hthk.calypsox.model.trade.criteria.CriteriaTrade;
import com.hthk.fintech.fintechservice.controller.FintechServiceController;
import com.hthk.fintech.fintechservice.service.RemoteServiceClient;
import com.hthk.fintech.model.data.datacenter.query.EntityTypeEnum;
import com.hthk.fintech.model.software.app.ApplicationEnum;
import com.hthk.fintech.model.software.app.ApplicationInstance;
import com.hthk.fintech.model.web.http.*;
import com.hthk.fintech.structure.utils.JacksonUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.hthk.fintech.config.FintechStaticData.LOG_DEFAULT;
import static com.hthk.fintech.config.FintechStaticData.LOG_WRAP;
import static org.junit.Assert.*;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/5 16:00
 */
public class RemoteServiceClientImplTest {

    private final static Logger logger = LoggerFactory.getLogger(RemoteServiceClientImplTest.class);

    RemoteServiceClient client = new RemoteServiceClientImpl();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetTrade_BY_TRADEFILTER() {

        HttpServiceRequest request = new HttpServiceRequest();
        IRequestAction<HttpRequestGetParams> action = new IRequestAction<>();
        action.setName(ActionTypeEnum.GET);
        HttpRequestGetParams params = new HttpRequestGetParams();
        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        params.setSource(instance);
        action.setParams(params);
        request.setAction(action);

        RequestDateTime dateTime = new RequestDateTime();
        request.setDateTime(dateTime);
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        RequestEntity requestEntity = new RequestEntity();
        request.setEntity(requestEntity);
        requestEntity.setType(EntityTypeEnum.TRADE);
        requestEntity.setSubType1("NA");
        requestEntity.setSubType2("NA");
        requestEntity.setSubType3("NA");
        requestEntity.setSubType4("NA");
        requestEntity.setSubType5("NA");

        CriteriaTrade criteria = new CriteriaTrade();
        request.setCriteria(criteria);

        criteria.setBookList(Arrays.asList("CIFXDH"));
        criteria.setTradeFilter("HTHK_FICC_TEST");

        logger.info(LOG_DEFAULT, "request", JacksonUtils.toJsonPrettyTry(request));

        Class clz = HttpResponse.class;

        HttpResponse resp = client.call(request, clz);
        logger.info(LOG_WRAP, "response", JacksonUtils.toJsonPrettyTry(resp));
    }

}