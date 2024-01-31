package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.trade.criteria.CriteriaTrade;
import com.hthk.calypsox.model.trade.product.FutureFXTradeInfo;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.fintechservice.comparator.BasicTradeInfoComparator;
import com.hthk.fintech.fintechservice.config.AppConfig;
import com.hthk.fintech.model.data.datacenter.query.EntityTypeEnum;
import com.hthk.fintech.model.software.app.ApplicationEnum;
import com.hthk.fintech.model.software.app.ApplicationInstance;
import com.hthk.fintech.model.trade.TradeInfo;
import com.hthk.fintech.model.web.http.*;
import com.hthk.fintech.structure.utils.JacksonUtils;
import com.hthk.fintech.utils.CSVUtils;
import com.hthk.fintech.utils.RemoteServiceUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static com.hthk.calypsox.config.CalypsoStaticData.ENV_NAME_FIRE_FIGHT;
import static com.hthk.calypsox.config.CalypsoStaticData.ENV_NAME_UAT;
import static com.hthk.fintech.config.FintechStaticData.LOG_DEFAULT;
import static com.hthk.fintech.config.FintechStaticData.LOG_WRAP;
import static org.junit.Assert.*;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/5 16:47
 */
public class RemoteTradeServiceTest {

    private final static Logger logger = LoggerFactory.getLogger(RemoteTradeServiceTest.class);

    RemoteTradeService remoteTradeService = new RemoteTradeService();

    RemoteStaticDataFutureService remoteStaticDataFutureService = new RemoteStaticDataFutureService();

    public RemoteTradeServiceTest() {
        RemoteServiceUtils.setup(remoteTradeService);
        RemoteServiceUtils.setup(remoteStaticDataFutureService);
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetTrade_BY_TRADEFILTER() throws ServiceInternalException {

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        instance.setInstance(ENV_NAME_UAT);

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        CriteriaTrade criteria = new CriteriaTrade();
//        criteria.setBookList(Arrays.asList("CIFXDH"));
//        criteria.setTradeFilter("HTHK_FICC_TEST");
        criteria.setTradeFilter("HTHK_FICC_MACRO_FXO_TEST_FutureFX");

        remoteTradeService.getTrade(instance, dateTime, criteria);
    }

    @Test
    public void testGetFutureFXBookCount_BY_TRADEFILTER() throws ServiceInternalException {

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        instance.setInstance(ENV_NAME_UAT);

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        CriteriaTrade criteria = new CriteriaTrade();
        criteria.setTradeFilter("HTHK_FICC_MACRO_FXO_TEST_FutureFX");

        List<TradeInfo> tradeInfoList = remoteTradeService.getTrade(instance, dateTime, criteria);
        Map<String, Integer> countMap = new HashMap<>();
        Set<String> keySet = new HashSet<>();

        tradeInfoList.forEach(t -> {
            String book = t.getBook();
            String pdType = t.getProductType();
            String pdSubType = t.getProductSubType();
            String underlying = t.getFutureUnderlyingTickerExchange();
            String key = book + ":" + pdType + ":" + pdSubType + ":" + underlying;
            if (!keySet.contains(key)) {
                keySet.add(key);
                countMap.put(key, 1);
            } else {
                int count = countMap.get(key);
                countMap.put(key, ++count);
            }
        });
        List<String> keyList = keySet.stream().collect(Collectors.toList());
        Collections.sort(keyList);
        logger.info(LOG_WRAP, "key", keyList.stream().map(t -> t + ":" + countMap.get(t)).collect(Collectors.joining("\r\n")));
    }

    @Test
    public void testGetFxTARF_BY_TRADEFILTER() throws ServiceInternalException, IOException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        String outputFile = "C:/Rock/Datas/IT/DEV_Datas/tmp/fxTARF.csv";

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        instance.setInstance(ENV_NAME_UAT);

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        CriteriaTrade criteria = new CriteriaTrade();
        criteria.setTradeFilter("HTHK_FICC_SOP");

        List<TradeInfo> tradeInfoList = remoteTradeService.getTrade(instance, dateTime, criteria);
        logger.info(LOG_WRAP, "futureInfo 1st", JacksonUtils.toJsonPrettyTry(tradeInfoList.get(0)));

        Collections.sort(tradeInfoList, new BasicTradeInfoComparator());

        CSVUtils.write(tradeInfoList, outputFile, "UTF-8", true, FutureFXTradeInfo.class);
    }

    @Test
    public void testGetFutureFXAndOutput() throws ServiceInternalException, IOException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        String outputFile = "C:\\Rock\\Datas\\IT\\DEV_Datas\\tmp\\tradeInfo.csv";

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        instance.setInstance(ENV_NAME_UAT);

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        CriteriaTrade criteria = new CriteriaTrade();
        criteria.setTradeFilter("HTHK_FICC_MACRO_FXO_TEST_FutureFX_testbook");

        List<TradeInfo> tradeInfoList = remoteTradeService.getTrade(instance, dateTime, criteria);
        logger.info(LOG_WRAP, "tradeInfoList 1st", JacksonUtils.toJsonPrettyTry(tradeInfoList.get(0)));

        CSVUtils.write(tradeInfoList, outputFile, "UTF-8", true, FutureFXTradeInfo.class);
    }

}