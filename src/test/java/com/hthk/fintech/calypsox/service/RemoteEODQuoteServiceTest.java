package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.marketdata.quote.eod.EODQuote;
import com.hthk.calypsox.model.marketdata.quote.eod.EODQuoteCompare;
import com.hthk.calypsox.model.quote.CriteriaEODQuote;
import com.hthk.common.utils.DateTimeUtils;
import com.hthk.fintech.exception.InvalidRequestException;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.fintechservice.comparator.EODQuoteComparator;
import com.hthk.fintech.model.software.app.ApplicationEnum;
import com.hthk.fintech.model.software.app.ApplicationInstance;
import com.hthk.fintech.model.web.http.RequestDateTime;
import com.hthk.fintech.service.FTPService;
import com.hthk.fintech.service.impl.FTPServiceImpl;
import com.hthk.fintech.structure.utils.JacksonUtils;
import com.hthk.fintech.utils.CSVUtils;
import com.hthk.fintech.utils.RemoteServiceUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.hthk.calypsox.config.CalypsoStaticData.ENV_NAME_UAT;
import static com.hthk.fintech.config.FintechStaticData.LOG_WRAP;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/15 18:02
 */
public class RemoteEODQuoteServiceTest {

    private final static Logger logger = LoggerFactory.getLogger(RemoteEODQuoteServiceTest.class);

    private FTPService ftpService = new FTPServiceImpl();

    RemoteEODQuoteService remoteEODQuoteService = new RemoteEODQuoteService();

    String outputFile;

    List<String> dateList = new ArrayList<>();

    public RemoteEODQuoteServiceTest() {

        RemoteServiceUtils.setup(remoteEODQuoteService);

//        dateList.add("2024-01-08");
//        dateList.add("2024-01-09");
//        dateList.add("2024-01-10");
//        dateList.add("2024-01-11");
//        dateList.add("2024-01-12");
//
//        dateList.add("2024-01-15");

    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetEODQuote_GENERAL() throws ServiceInternalException, IOException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InvalidRequestException {

        String startStr = "2024-03-18";
        LocalDate today = LocalDate.now();

        LocalDate start = LocalDate.parse(startStr, DateTimeFormatter.ISO_DATE);
        List<LocalDate> busDateList = DateTimeUtils.generateBusinessDate(start, today, null, null);

        busDateList.forEach(
                t -> {
                    try {
                        process(t);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private void process(LocalDate date) throws ServiceInternalException, IOException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        String dateStr = date.format(DateTimeFormatter.ISO_DATE);
        outputFile = "M:/Prod_Files/MarketData/EODQuote/EODQuote_" + dateStr + ".csv";

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
        criteria.setDateList(Arrays.asList(date));
//        criteria.setQuoteName("FX.USD.HKD");

        List<EODQuote> quoteList = remoteEODQuoteService.getQuote(instance, dateTime, criteria);

        logger.info("end {}", LocalDateTime.now());

        logger.info(LOG_WRAP, "1st in list", JacksonUtils.toJsonPrettyTry(quoteList.get(0)));

        CSVUtils.write(quoteList, outputFile, "UTF-8", true, EODQuote.class);

    }

    @Test
    public void generateEODQuoteDailyDiff() throws ServiceInternalException, IOException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        String dateStr = getDateStr();
        outputFile = "C:/Rock/Datas/IT/DEV_Datas/tmp/EOD_Quote_Daily_Diff_" + dateStr + ".csv";
        String remoteFolder = "/home/calypso/HTHK_SHARE";

        String endDate = "2024-03-11";
        String startDate = "2024-03-08";

        String serverIP = "168.64.17.87";
        String ftpUser = "calypso";
        String ftpPwd = "App@Admin123";

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        instance.setInstance(ENV_NAME_UAT);

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        List<String> quoteNameList = getQuoteNameList();

        CriteriaEODQuote criteria = new CriteriaEODQuote();
        criteria.setQuoteNameList(quoteNameList);
        criteria.setDateList(Arrays.asList(
                LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE), LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE)
        ));

        List<EODQuote> quoteList = remoteEODQuoteService.getQuote(instance, dateTime, criteria);

        List<EODQuoteCompare> diffList = convert(quoteList);
        CSVUtils.write(diffList, outputFile, "UTF-8", true, EODQuoteCompare.class);

        logger.info("send to share folder: {} {} {}", new File(outputFile).getName(), remoteFolder, serverIP);
        ftpService.send(outputFile, remoteFolder, serverIP, ftpUser, ftpPwd);

    }

    @Test
    public void generateEODJPYQuoteDailyDiff() throws ServiceInternalException, IOException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        String dateStr = getDateStr();
        outputFile = "C:/Rock/Datas/IT/DEV_Datas/tmp/EOD_Quote_Daily_Diff_" + dateStr + ".csv";
        String remoteFolder = "/home/calypso/HTHK_SHARE";

        String endDate = "2024-03-15";
        String startDate = "2024-03-14";

        String serverIP = "168.64.17.87";
        String ftpUser = "calypso";
        String ftpPwd = "App@Admin123";

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        instance.setInstance(ENV_NAME_UAT);

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        List<String> quoteNameList = getJPYQuoteNameList();

        CriteriaEODQuote criteria = new CriteriaEODQuote();
        criteria.setQuoteNameList(quoteNameList);
        criteria.setDateList(Arrays.asList(
                LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE), LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE)
        ));

        List<EODQuote> quoteList = remoteEODQuoteService.getQuote(instance, dateTime, criteria);

        List<EODQuoteCompare> diffList = convert(quoteList);
        CSVUtils.write(diffList, outputFile, "UTF-8", true, EODQuoteCompare.class);

        logger.info("send to share folder: {} {} {}", new File(outputFile).getName(), remoteFolder, serverIP);
        ftpService.send(outputFile, remoteFolder, serverIP, ftpUser, ftpPwd);

    }

    private List<EODQuoteCompare> convert(List<EODQuote> quoteList) {

        List<EODQuoteCompare> allList = new ArrayList<>();

        Map<String, List<EODQuote>> quoteMap = new HashMap<>();
        quoteList.forEach(q -> {
            String quoteName = q.getQuoteName();
            List<EODQuote> list = quoteMap.get(quoteName) == null ? new ArrayList<>() : quoteMap.get(quoteName);
            quoteMap.put(quoteName, list);
            list.add(q);
        });

        quoteMap.forEach((k, v) -> {
            EODQuoteCompare dailyDiff = convert(k, v);
            allList.add(dailyDiff);
        });
        return allList;
    }

    private EODQuoteCompare convert(String quoteName, List<EODQuote> quoteList) {

        Collections.sort(quoteList, new EODQuoteComparator());

        EODQuote quoteStart = quoteList.get(0);
        EODQuote quoteEnd = quoteList.get(1);
        BigDecimal diff = quoteEnd.getClose().subtract(quoteStart.getClose());

        return new EODQuoteCompare(quoteName, quoteStart.getQuoteType(), quoteEnd.getClose(), quoteStart.getClose(), diff.toPlainString(), quoteEnd.getDate(), quoteStart.getDate());
    }

    private List<String> getJPYQuoteNameList() {

        List<String> quoteNameList = new ArrayList<>();

        quoteNameList.add("MM.JPY.TONAR.ON.BOJDTR");
        quoteNameList.add("Swap.1W.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.2W.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.3W.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.1M.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.2M.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.3M.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.4M.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.5M.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.6M.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.9M.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.1Y.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.18M.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.2Y.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.3Y.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.4Y.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.5Y.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.6Y.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.7Y.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.8Y.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.9Y.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.10Y.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.12Y.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.15Y.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.20Y.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.25Y.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.30Y.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.35Y.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Swap.40Y.JPY.TONAR.1D/1Y.BOJDTR");
        quoteNameList.add("Spread.Swap.LCH_JSCC.1W");
        quoteNameList.add("Spread.Swap.LCH_JSCC.2W");
        quoteNameList.add("Spread.Swap.LCH_JSCC.3W");
        quoteNameList.add("Spread.Swap.LCH_JSCC.1M");
        quoteNameList.add("Spread.Swap.LCH_JSCC.2M");
        quoteNameList.add("Spread.Swap.LCH_JSCC.3M");
        quoteNameList.add("Spread.Swap.LCH_JSCC.4M");
        quoteNameList.add("Spread.Swap.LCH_JSCC.5M");
        quoteNameList.add("Spread.Swap.LCH_JSCC.6M");
        quoteNameList.add("Spread.Swap.LCH_JSCC.9M");
        quoteNameList.add("Spread.Swap.LCH_JSCC.1Y");
        quoteNameList.add("Spread.Swap.LCH_JSCC.18M");
        quoteNameList.add("Spread.Swap.LCH_JSCC.2Y");
        quoteNameList.add("Spread.Swap.LCH_JSCC.3Y");
        quoteNameList.add("Spread.Swap.LCH_JSCC.4Y");
        quoteNameList.add("Spread.Swap.LCH_JSCC.5Y");
        quoteNameList.add("Spread.Swap.LCH_JSCC.6Y");
        quoteNameList.add("Spread.Swap.LCH_JSCC.7Y");
        quoteNameList.add("Spread.Swap.LCH_JSCC.8Y");
        quoteNameList.add("Spread.Swap.LCH_JSCC.9Y");
        quoteNameList.add("Spread.Swap.LCH_JSCC.10Y");
        quoteNameList.add("Spread.Swap.LCH_JSCC.12Y");
        quoteNameList.add("Spread.Swap.LCH_JSCC.15Y");
        quoteNameList.add("Spread.Swap.LCH_JSCC.20Y");
        quoteNameList.add("Spread.Swap.LCH_JSCC.25Y");
        quoteNameList.add("Spread.Swap.LCH_JSCC.30Y");
        quoteNameList.add("Spread.Swap.LCH_JSCC.35Y");
        quoteNameList.add("Spread.Swap.LCH_JSCC.40Y");

        return quoteNameList;
    }

    private List<String> getQuoteNameList() {

        List<String> quoteNameList = new ArrayList<>();

        quoteNameList.add("FX.USD.CNH");
        quoteNameList.add("FX.USD.CNH.ON");
        quoteNameList.add("FX.USD.CNH.1W");
        quoteNameList.add("FX.USD.CNH.2W");
        quoteNameList.add("FXOption.USD/CNH.2W.ATM");
        quoteNameList.add("FX.USD.HKD");

        return quoteNameList;
    }

    private String getDateStr() {
        return LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }

}