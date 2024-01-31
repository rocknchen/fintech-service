package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.trade.criteria.CriteriaTrade;
import com.hthk.calypsox.model.trade.product.FutureFXTradeInfo;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.fintechservice.converter.impl.FutureFXTradeInfoConverterImpl;
import com.hthk.fintech.model.software.app.ApplicationEnum;
import com.hthk.fintech.model.software.app.ApplicationInstance;
import com.hthk.fintech.model.web.http.RequestDateTime;
import com.hthk.fintech.structure.utils.JacksonUtils;
import com.hthk.fintech.utils.CSVUtils;
import com.hthk.fintech.utils.RemoteServiceUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.hthk.calypsox.config.CalypsoStaticData.ENV_NAME_UAT;
import static com.hthk.fintech.config.FintechStaticData.LOG_WRAP;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/9 21:16
 */
public class RemoteTradeFutureFXServiceTest {

    private final static Logger logger = LoggerFactory.getLogger(RemoteTradeServiceTest.class);

    RemoteTradeService remoteTradeService = new RemoteTradeService();

    RemoteStaticDataFutureService remoteStaticDataFutureService = new RemoteStaticDataFutureService();

    RemoteTradeFutureFXService remoteTradeFutureFXService = new RemoteTradeFutureFXService();

    FutureFXTradeInfoConverterImpl converter = new FutureFXTradeInfoConverterImpl();

    String outputFile;

    public RemoteTradeFutureFXServiceTest() {

        RemoteServiceUtils.setup(remoteTradeService);
        RemoteServiceUtils.setup(remoteStaticDataFutureService);
        RemoteServiceUtils.setup(remoteTradeFutureFXService);

        remoteTradeFutureFXService.setRemoteStaticDataFutureService(remoteStaticDataFutureService);
        remoteTradeFutureFXService.setConverter(converter);

        outputFile = "C:/Rock/Datas/IT/DEV_Datas/tmp/futureFXTradeInfo.csv";
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetFutureFXTradeAndOutput() throws ServiceInternalException, IOException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        instance.setInstance(ENV_NAME_UAT);

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        CriteriaTrade criteria = new CriteriaTrade();
//        criteria.setBookList(Arrays.asList("CIFXDH"));
        criteria.setTradeFilter("HTHK_FICC_MACRO_FXO_TEST_FutureFX");

        List<FutureFXTradeInfo> futureInfoList = remoteTradeFutureFXService.getTrade(instance, dateTime, criteria);
        logger.info(LOG_WRAP, "futureInfo 1st", JacksonUtils.toJsonPrettyTry(futureInfoList.get(0)));

        CSVUtils.write(futureInfoList, outputFile, "UTF-8", true, FutureFXTradeInfo.class);
    }

    @Test
    public void testGetFutureFXTradeTestAndOutput() throws ServiceInternalException, IOException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        instance.setInstance(ENV_NAME_UAT);

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        CriteriaTrade criteria = new CriteriaTrade();
//        criteria.setBookList(Arrays.asList("CIFXDH"));
        criteria.setTradeFilter("HTHK_FICC_MACRO_FXO_TEST_FutureFX_testbook");

        List<FutureFXTradeInfo> futureInfoList = remoteTradeFutureFXService.getTrade(instance, dateTime, criteria);
        logger.info(LOG_WRAP, "futureInfo 1st", JacksonUtils.toJsonPrettyTry(futureInfoList.get(0)));

        CSVUtils.write(futureInfoList, outputFile, "UTF-8", true, FutureFXTradeInfo.class);
    }

}