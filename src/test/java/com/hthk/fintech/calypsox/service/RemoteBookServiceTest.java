package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.staticdata.book.criteria.CriteriaBook;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.model.software.app.ApplicationEnum;
import com.hthk.fintech.model.software.app.ApplicationInstance;
import com.hthk.fintech.model.staticdata.BookInfo;
import com.hthk.fintech.model.staticdata.BookInfoVO1;
import com.hthk.fintech.model.web.http.RequestDateTime;
import com.hthk.fintech.structure.utils.JacksonUtils;
import com.hthk.fintech.utils.CSVUtils;
import com.hthk.fintech.utils.RemoteServiceUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.hthk.calypsox.config.CalypsoStaticData.ENV_NAME_UAT;
import static com.hthk.fintech.config.FintechStaticData.LOG_WRAP;

/**
 * @Author: Rock CHEN
 * @Date: 2024/2/20 15:43
 */
public class RemoteBookServiceTest {

    private final static Logger logger = LoggerFactory.getLogger(RemoteBookServiceTest.class);

    RemoteBookService remoteBookService = new RemoteBookService();
    RemoteStaticDataFutureService remoteStaticDataFutureService = new RemoteStaticDataFutureService();

    public RemoteBookServiceTest() {
        RemoteServiceUtils.setup(remoteBookService);
        RemoteServiceUtils.setup(remoteStaticDataFutureService);
    }

    @Test
    public void testGetBook() throws ServiceInternalException, IOException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        String outputFile = "C:\\Rock\\Datas\\IT\\DEV_Datas\\tmp\\bookInfo.csv";

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        instance.setInstance(ENV_NAME_UAT);

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        CriteriaBook criteria = new CriteriaBook();
        criteria.setNamePreList(Arrays.asList("CIFXDH_TEST", "CIFXFI_TEST", "FUTURE_FX_CASH_POS_POC"));
//        criteria.setIdStart(1);
//        criteria.setIdEnd(30);

        List<BookInfo> bookInfoList = remoteBookService.getBook(instance, dateTime, criteria);
        logger.info(LOG_WRAP, "bookInfoList 1st", JacksonUtils.toJsonPrettyTry(bookInfoList.get(0)));

        CSVUtils.write(bookInfoList, outputFile, "UTF-8", true, BookInfo.class);
    }

    @Test
    public void testGetHKBook() throws ServiceInternalException, IOException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        String outputFile = "C:\\Rock\\Datas\\IT\\DEV_Datas\\tmp\\bookInfo.csv";

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        instance.setInstance(ENV_NAME_UAT);

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        CriteriaBook criteria = new CriteriaBook();
        criteria.setLegalEntityList(Arrays.asList("HKFH", "HKCI", "HTIFP", "HTSG"));
        criteria.setCompanyShortNameList(Arrays.asList("FICC", "TRES"));
//        criteria.setIdStart(1);
//        criteria.setIdEnd(30);

        List<BookInfo> bookInfoList = remoteBookService.getBook(instance, dateTime, criteria);
        List<BookInfoVO1> bookInfoVO1List = bookInfoList.stream().map(t ->
                {
                    BookInfoVO1 vo = new BookInfoVO1();
                    BeanUtils.copyProperties(t, vo);
                    return vo;
                }
        ).collect(Collectors.toList());
        logger.info(LOG_WRAP, "bookInfoList 1st", JacksonUtils.toJsonPrettyTry(bookInfoVO1List.get(0)));

        CSVUtils.write(bookInfoVO1List, outputFile, "UTF-8", true, BookInfoVO1.class);
    }

}