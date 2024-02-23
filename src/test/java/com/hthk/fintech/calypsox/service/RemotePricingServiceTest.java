package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.pricing.criteria.CriteriaDateRangePricing;
import com.hthk.calypsox.model.trade.product.FutureFXTradeInfo;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.model.datetime.DateRange;
import com.hthk.fintech.model.pricing.TradePricingResultInfo;
import com.hthk.fintech.model.software.app.ApplicationEnum;
import com.hthk.fintech.model.software.app.ApplicationInstance;
import com.hthk.fintech.model.web.http.RequestDateTime;
import com.hthk.fintech.structure.utils.JacksonUtils;
import com.hthk.fintech.utils.CSVUtils;
import com.hthk.fintech.utils.RemoteServiceUtils;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.hthk.calypsox.config.CalypsoStaticData.ENV_NAME_UAT;
import static com.hthk.fintech.config.FintechStaticData.LOG_WRAP;

/**
 * @Author: Rock CHEN
 * @Date: 2024/2/23 15:55
 */
public class RemotePricingServiceTest {

    private final static Logger logger = LoggerFactory.getLogger(RemotePricingServiceTest.class);

    private RemotePricingService remotePricingService = new RemotePricingService();

    private String outputFile;

    public RemotePricingServiceTest() {

        RemoteServiceUtils.setup(remotePricingService);
        outputFile = "C:/Rock/Datas/IT/DEV_Datas/tmp/pricingResult";
    }

    @Test
    public void testGetFutureCommTradeAndOutputTest() throws ServiceInternalException, IOException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        int tradeId = 8087515;
        String start = "2024-01-19";
        String end = "2024-01-29";
        List<String> measureList = new ArrayList<>();
        measureList.add("CA_NOTIONAL");
        measureList.add("PV01");

        String tmpOutputFile = outputFile + "_" + tradeId + ".csv";

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        instance.setInstance(ENV_NAME_UAT);

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        CriteriaDateRangePricing criteria = new CriteriaDateRangePricing();
        criteria.setTradeId(Long.valueOf(tradeId));
        criteria.setValuationTime("23:59:59");
        criteria.setDateRange(new DateRange(LocalDate.parse(start, DateTimeFormatter.ISO_DATE), LocalDate.parse(end, DateTimeFormatter.ISO_DATE)));
        criteria.setPricingMeasureList(measureList);

        List<TradePricingResultInfo> pricingResultList = remotePricingService.price(instance, dateTime, criteria);
        if (!CollectionUtils.isEmpty(pricingResultList)) {
            logger.info(LOG_WRAP, "futureInfo 1st", JacksonUtils.toJsonPrettyTry(pricingResultList.get(0)));
//            Collections.sort(futureInfoList, new FutureFXTradeInfoComparator());
            CSVUtils.write(pricingResultList, tmpOutputFile, "UTF-8", true, FutureFXTradeInfo.class);
        } else {
            new File(outputFile).deleteOnExit();
        }

    }

}