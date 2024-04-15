package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.person.UserInfo;
import com.hthk.calypsox.model.person.criteria.CriteriaUser;
import com.hthk.calypsox.model.staticdata.book.BookAccess;
import com.hthk.calypsox.model.staticdata.book.BookAccessOrig;
import com.hthk.calypsox.model.staticdata.book.BookTestInfo;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.model.software.app.ApplicationEnum;
import com.hthk.fintech.model.software.app.ApplicationInstance;
import com.hthk.fintech.model.web.http.RequestDateTime;
import com.hthk.fintech.structure.utils.JacksonUtils;
import com.hthk.fintech.utils.CSVUtils;
import com.hthk.fintech.utils.RemoteServiceUtils;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static com.hthk.calypsox.config.CalypsoStaticData.ENV_NAME_UAT;
import static com.hthk.fintech.config.FintechStaticData.LOG_WRAP;

/**
 * @Author: Rock CHEN
 * @Date: 2024/4/15 17:39
 */
public class RemoteUserServiceTest {

    private final static Logger logger = LoggerFactory.getLogger(RemoteUserServiceTest.class);

    RemoteUserService remoteUserService = new RemoteUserService();

    List<BookTestInfo> bookTestInfoList = new ArrayList<>();

    @Before
    public void setUp() throws Exception {

        int num = 1;
        bookTestInfoList.add(new BookTestInfo(num++, "CIFXDH", "FX Option"));
        bookTestInfoList.add(new BookTestInfo(num++, "CIMFH", "FX Option"));
        bookTestInfoList.add(new BookTestInfo(num++, "CISF", "FX Option"));
        bookTestInfoList.add(new BookTestInfo(num++, "CISP", "FX Option"));
        bookTestInfoList.add(new BookTestInfo(num++, "CISR", "FX Option"));
        bookTestInfoList.add(new BookTestInfo(num++, "CISRH", "FX Option"));
        bookTestInfoList.add(new BookTestInfo(num++, "CIFXFI", "FX Option"));
        bookTestInfoList.add(new BookTestInfo(num++, "FHFXD", "FX Option"));
        bookTestInfoList.add(new BookTestInfo(num++, "FHFXDBTB", "FX Option"));
        bookTestInfoList.add(new BookTestInfo(num++, "CIFXD", "FX Option"));
        bookTestInfoList.add(new BookTestInfo(num++, "CIMD", "FX Option"));
        bookTestInfoList.add(new BookTestInfo(num++, "CIMDH", "FX Option"));
        bookTestInfoList.add(new BookTestInfo(num++, "CIMMH", "FX Option"));
        bookTestInfoList.add(new BookTestInfo(num++, "CIFXFIH", "FX Option"));
        bookTestInfoList.add(new BookTestInfo(num++, "CIPMD", "FX Option"));
        bookTestInfoList.add(new BookTestInfo(num++, "CIPMDH", "FX Option"));

        bookTestInfoList.add(new BookTestInfo(num++, "CICRDM", "Passthrough"));
        bookTestInfoList.add(new BookTestInfo(num++, "CIDYPUPN", "Passthrough"));
        bookTestInfoList.add(new BookTestInfo(num++, "CIHYOFFB", "Passthrough"));
        bookTestInfoList.add(new BookTestInfo(num++, "CIOFFB", "Passthrough"));
        bookTestInfoList.add(new BookTestInfo(num++, "CIPN", "Passthrough"));
        bookTestInfoList.add(new BookTestInfo(num++, "CIPNCOMM", "Passthrough"));
        bookTestInfoList.add(new BookTestInfo(num++, "CIPNFX", "Passthrough"));

    }

    public RemoteUserServiceTest() {
        RemoteServiceUtils.setup(remoteUserService);
    }

    @Test
    public void testGetBookAndUser_DEFAULT() throws IOException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, ServiceInternalException {

        String outputFile = "C:/Rock/Datas/IT/DEV_Datas/tmp/bookAccessUsers.csv";

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        instance.setInstance(ENV_NAME_UAT);

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        CriteriaUser criteria = new CriteriaUser();
//        criteria.setUserNameList(Arrays.asList("test1", "017522", "chenr", "test2"));

        List<UserInfo> userInfoList = remoteUserService.getUserInfoList(instance, dateTime, criteria);
        logger.info(LOG_WRAP, "userInfo 1st", JacksonUtils.toJsonPrettyTry(userInfoList.get(0)));

        Map<String, String> bookCatMap = create(bookTestInfoList);
        logger.info("bookCatMap {}", bookCatMap);

        List<String> bookList = generate(bookTestInfoList);
        List<BookAccessOrig> bookAccessOrigList = convert(userInfoList, bookList);
        List<BookAccess> bookAccessList = convert(bookAccessOrigList, bookCatMap);

        CSVUtils.write(bookAccessList, outputFile, "UTF-8", true, BookAccess.class);
    }

    private Map<String, String> create(List<BookTestInfo> bookTestInfoList) {
        Map<String, String> map = new HashedMap();
        bookTestInfoList.stream().forEach(t -> map.put(t.getBookName(), t.getCategory()));
        return map;
    }

    private List<BookAccess> convert(List<BookAccessOrig> bookAccessOrigList, Map<String, String> bookCatMap) {
        return bookAccessOrigList.stream().map(t -> convert(t, bookCatMap)).collect(Collectors.toList());
    }

    private BookAccess convert(BookAccessOrig bookAccessOrig, Map<String, String> bookCatMap) {
        BookAccess bookAccess = new BookAccess();
        BeanUtils.copyProperties(bookAccessOrig, bookAccess);
        bookAccess.setBookCategory(bookCatMap.get(bookAccessOrig.getBookName()));
        bookAccess.setAccessDesc(Optional.ofNullable(bookAccessOrig.getAccessList()).map(t -> t.stream().collect(Collectors.joining(", "))).orElse(null));
        return bookAccess;
    }

    private List<BookAccessOrig> convert(List<UserInfo> userInfoList, List<String> bookList) {
        List<BookAccessOrig> bookAccessOrigList = new ArrayList<>();
        bookList.stream().forEach(bookName -> {
            List<BookAccessOrig> accessOrigList = convert(userInfoList, bookName);
            bookAccessOrigList.addAll(accessOrigList);
        });
        return bookAccessOrigList;
    }

    private List<BookAccessOrig> convert(List<UserInfo> userInfoList, String bookName) {
        List<BookAccessOrig> list = new ArrayList<>();
        BookAccessOrig readOnly = new BookAccessOrig(bookName, "ReadOnly");
        BookAccessOrig readWrite = new BookAccessOrig(bookName, "Read and Write");
        list.add(readWrite);
        list.add(readOnly);
        userInfoList.forEach(u -> {
            List<String> bookReadWriteList = u.getBookReadWrite();
            List<String> bookReadOnlyList = u.getBookReadOnly();
            if (bookReadWriteList != null && bookReadWriteList.contains(bookName)) {
                readWrite.addAccess(u.getFullName());
            } else if (bookReadOnlyList != null && bookReadOnlyList.contains(bookName)) {
                readOnly.addAccess(u.getFullName());
            }
        });
        return list;
    }

    private List<String> generate(List<BookTestInfo> bookTestInfoList) {
        return bookTestInfoList.stream().map(t -> t.getBookName()).collect(Collectors.toList());
    }

}