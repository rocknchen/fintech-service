package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.marketdata.quote.eod.EODQuote;
import com.hthk.calypsox.model.quote.CriteriaEODQuote;
import com.hthk.calypsox.model.trade.criteria.CriteriaTrade;
import com.hthk.calypsox.model.trade.product.FutureFXTradeInfo;
import com.hthk.fintech.exception.ServiceInternalException;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.hthk.calypsox.config.CalypsoStaticData.ENV_NAME_UAT;
import static com.hthk.fintech.config.FintechStaticData.LOG_WRAP;
import static org.junit.Assert.*;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/15 18:02
 */
public class RemoteEODQuoteServiceTest {

    private final static Logger logger = LoggerFactory.getLogger(RemoteEODQuoteServiceTest.class);

    RemoteEODQuoteService remoteEODQuoteService = new RemoteEODQuoteService();

    String outputFile;

    List<String> dateList = new ArrayList<>();

    public RemoteEODQuoteServiceTest() {

        RemoteServiceUtils.setup(remoteEODQuoteService);

        dateList.add("2024-01-08");
        dateList.add("2024-01-09");
        dateList.add("2024-01-10");
        dateList.add("2024-01-11");
        dateList.add("2024-01-12");

        dateList.add("2024-01-15");

    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetEODQuote_GENERAL() throws ServiceInternalException, IOException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        dateList.forEach(
                t -> {
                    try {
                        process(t);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private void process(String date) throws ServiceInternalException, IOException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        outputFile = "M:/Prod_Files/" + date + ".csv";

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        instance.setInstance(ENV_NAME_UAT);

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        CriteriaEODQuote criteria = new CriteriaEODQuote();
//        criteria.setBookList(Arrays.asList("CIFXDH"));
        logger.info("start {}", LocalDateTime.now());
//        criteria.setDate(LocalDate.parse("2023-01-02", DateTimeFormatter.ISO_DATE));
//        criteria.setDateList(Arrays.asList(LocalDate.parse("2023-01-03", DateTimeFormatter.ISO_DATE), LocalDate.parse("2023-01-01", DateTimeFormatter.ISO_DATE)));
        criteria.setDateList(Arrays.asList(LocalDate.parse(date, DateTimeFormatter.ISO_DATE)));
//        criteria.setQuoteName("FX.USD.HKD");

        List<EODQuote> quoteList = remoteEODQuoteService.getQuote(instance, dateTime, criteria);

        logger.info("end {}", LocalDateTime.now());

        logger.info(LOG_WRAP, "1st in list", JacksonUtils.toJsonPrettyTry(quoteList.get(0)));

        CSVUtils.write(quoteList, outputFile, "UTF-8", true, EODQuote.class);

    }

}