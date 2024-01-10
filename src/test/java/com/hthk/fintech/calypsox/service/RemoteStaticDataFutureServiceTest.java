package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.staticdata.future.contract.FutureInfo;
import com.hthk.calypsox.model.staticdata.future.contract.criteria.CriteriaFuture;
import com.hthk.calypsox.model.trade.criteria.CriteriaTrade;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.fintechservice.config.AppConfig;
import com.hthk.fintech.model.software.app.ApplicationEnum;
import com.hthk.fintech.model.software.app.ApplicationInstance;
import com.hthk.fintech.model.trade.TradeInfo;
import com.hthk.fintech.model.web.http.RequestDateTime;
import com.hthk.fintech.structure.utils.JacksonUtils;
import com.hthk.fintech.utils.RemoteServiceUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static com.hthk.calypsox.config.CalypsoStaticData.ENV_NAME_FIRE_FIGHT;
import static com.hthk.calypsox.config.CalypsoStaticData.ENV_NAME_UAT;
import static com.hthk.fintech.config.FintechStaticData.LOG_WRAP;
import static org.junit.Assert.*;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/9 20:34
 */
public class RemoteStaticDataFutureServiceTest {

    private final static Logger logger = LoggerFactory.getLogger(RemoteStaticDataFutureServiceTest.class);

    RemoteStaticDataFutureService remoteStaticDataFutureService = new RemoteStaticDataFutureService();

    public RemoteStaticDataFutureServiceTest() {
        RemoteServiceUtils.setup(remoteStaticDataFutureService);
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetFuture_UCA() throws ServiceInternalException {

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        instance.setInstance(ENV_NAME_UAT);

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        CriteriaFuture criteria = new CriteriaFuture();
        criteria.setExchange("HKEX");
        criteria.setName("UCA");
        criteria.setCurrency("CNH");
        criteria.setExpirationStart(LocalDate.parse("2020-01-01", DateTimeFormatter.ISO_DATE));

        List<FutureInfo> futureInfoList = remoteStaticDataFutureService.getFuture(instance, dateTime, criteria);
        logger.info(LOG_WRAP, "futureInfoList", JacksonUtils.toJsonPrettyTry(futureInfoList));
    }
}