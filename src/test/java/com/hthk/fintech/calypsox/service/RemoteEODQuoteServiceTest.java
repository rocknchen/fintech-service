package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.marketdata.quote.eod.EODQuote;
import com.hthk.calypsox.model.marketdata.quote.eod.EODQuoteCompare;
import com.hthk.calypsox.model.quote.CriteriaEODQuote;
import com.hthk.calypsox.model.trade.criteria.CriteriaTrade;
import com.hthk.calypsox.model.trade.product.FutureFXTradeInfo;
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
import org.apache.commons.net.ftp.FTP;
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
import java.util.spi.CalendarDataProvider;
import java.util.stream.Collectors;

import static com.hthk.calypsox.config.CalypsoStaticData.ENV_NAME_UAT;
import static com.hthk.fintech.config.FintechStaticData.LOG_WRAP;
import static org.junit.Assert.*;

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