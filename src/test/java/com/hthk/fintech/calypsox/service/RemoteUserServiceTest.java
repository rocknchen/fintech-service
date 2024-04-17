package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.person.UserInfo;
import com.hthk.calypsox.model.person.UserInfoVO;
import com.hthk.calypsox.model.person.criteria.CriteriaUser;
import com.hthk.calypsox.model.staticdata.book.BookAccess;
import com.hthk.calypsox.model.staticdata.book.BookAccessALl;
import com.hthk.calypsox.model.staticdata.book.BookAccessOrig;
import com.hthk.calypsox.model.staticdata.book.BookTestInfo;
import com.hthk.calypsox.model.staticdata.book.criteria.CriteriaBook;
import com.hthk.common.utils.FileUtils;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.fintechservice.comparator.BasicBookAccessALLComparator;
import com.hthk.fintech.fintechservice.comparator.BasicBookInfoComparator;
import com.hthk.fintech.model.software.app.ApplicationEnum;
import com.hthk.fintech.model.software.app.ApplicationInstance;
import com.hthk.fintech.model.staticdata.BookInfo;
import com.hthk.fintech.model.web.http.RequestDateTime;
import com.hthk.fintech.structure.utils.JacksonUtils;
import com.hthk.fintech.utils.CSVUtils;
import com.hthk.fintech.utils.RemoteServiceUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static com.hthk.calypsox.config.CalypsoStaticData.ENV_NAME_PROD;
import static com.hthk.calypsox.config.CalypsoStaticData.ENV_NAME_UAT;
import static com.hthk.fintech.config.FintechStaticData.LOG_WRAP;

/**
 * @Author: Rock CHEN
 * @Date: 2024/4/15 17:39
 */
public class RemoteUserServiceTest {

    private final static Logger logger = LoggerFactory.getLogger(RemoteUserServiceTest.class);

    RemoteUserService remoteUserService = new RemoteUserService();

    RemoteBookService remoteBookService = new RemoteBookService();

    List<BookTestInfo> bookTestInfoList = new ArrayList<>();

    List<String> ficcGroupList = new ArrayList<>();

    List<String> ignoreUserList = new ArrayList<>();

    List<String> ignoreBookList = new ArrayList<>();

    @Before
    public void setUp() throws Exception {

        String ficcBookFile = "C:/Rock/Datas/Docs/HTS/jira/cal1345/ficc_books.csv";

        String ignoreUserFile = "C:/Rock/Datas/Docs/HTS/jira/cal1345/ignoreUser.csv";

        String ignoreBookFile = "C:/Rock/Datas/Docs/HTS/jira/cal1345/ignoreBook.csv";

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

        ficcGroupList.addAll(FileUtils.readResourceAsStrList(new File(ficcBookFile)));
        ignoreUserList.addAll(FileUtils.readResourceAsStrList(new File(ignoreUserFile)));
        ignoreBookList.addAll(FileUtils.readResourceAsStrList(new File(ignoreBookFile)));

    }

    public RemoteUserServiceTest() {
        RemoteServiceUtils.setup(remoteUserService);
        RemoteServiceUtils.setup(remoteBookService);
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

    @Test
    public void testGetAllUserAccess_DEFAULT() throws IOException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, ServiceInternalException {

        String outputFile = "C:/Rock/Datas/IT/DEV_Datas/tmp/allLUserAccess.csv";

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        instance.setInstance(ENV_NAME_UAT);

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        CriteriaUser criteria = new CriteriaUser();

        List<UserInfo> userInfoList = remoteUserService.getUserInfoList(instance, dateTime, criteria);
        logger.info(LOG_WRAP, "userInfo 1st", JacksonUtils.toJsonPrettyTry(userInfoList.get(0)));

        List<UserInfoVO> voList = convert(userInfoList);

        CSVUtils.write(voList, outputFile, "UTF-8", true, UserInfoVO.class);
    }

    @Test
    public void testGetAccessOfFICCBooks() throws IOException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, ServiceInternalException {

        String outputFile = "C:/Rock/Datas/IT/DEV_Datas/tmp/allFICCUserAccess.csv";

        String instanceName = ENV_NAME_PROD;

        List<BookInfo> ficcBookList = getFICCBookList(instanceName);
        logger.info("ficcBookList: {}", JacksonUtils.toYMLPrettyTry(ficcBookList.get(0)));
        logger.info("count: {}", ficcBookList.size());
        Collections.sort(ficcBookList, new BasicBookInfoComparator());

        List<UserInfo> userInfoList = getALlFICCUserList(instanceName, ficcGroupList);
        logger.info("userInfoList: {}", userInfoList.size());

        List<BookInfo> filterFiccBookList = filterBook(ficcBookList, ignoreBookList);
        filterFiccBookList = filterBookMirror(filterFiccBookList);

        List<BookAccessALl> bookAccessALlList = generate(filterFiccBookList, userInfoList);
        Collections.sort(bookAccessALlList, new BasicBookAccessALLComparator());

        CSVUtils.write(bookAccessALlList, outputFile, "UTF-8", true, UserInfoVO.class);
    }

    private List<BookInfo> filterBookMirror(List<BookInfo> filterFiccBookList) {
        return filterFiccBookList.stream().filter(t -> !t.getName().endsWith(" MIRROR")).collect(Collectors.toList());
    }

    private List<BookInfo> filterBook(List<BookInfo> ficcBookList, List<String> ignoreBookList) {
        return ficcBookList.stream().filter(b -> !ignoreBookList.contains(b.getName())).collect(Collectors.toList());
    }

    private List<BookAccessALl> generate(List<BookInfo> ficcBookList, List<UserInfo> userInfoList) {
        List<BookAccessALl> bookAccessALlList = new ArrayList<>();
        ficcBookList.forEach(t -> bookAccessALlList.addAll(generate(t, userInfoList)));
        return bookAccessALlList;
    }

    private List<BookAccessALl> generate(BookInfo bookInfo, List<UserInfo> userInfoList) {
        List<BookAccessALl> list = userInfoList.stream().map(ui -> generate(bookInfo, ui)).collect(Collectors.toList());
        return list.stream().filter(t -> t != null).collect(Collectors.toList());
    }

    private BookAccessALl generate(BookInfo bookInfo, UserInfo ui) {
        List<String> bookReadWriteList = ui.getBookReadWrite();
        List<String> bookReadOnlyList = ui.getBookReadOnly();
        String bookName = bookInfo.getName();
        if (bookReadWriteList.contains(bookName) && !ignoreUserList.contains(ui.getUserName())) {
            return generateBookAccessAll(bookInfo, ui, "Read and Write");
        } else if (bookReadOnlyList.contains(bookName) && !ignoreUserList.contains(ui.getUserName())) {
            return generateBookAccessAll(bookInfo, ui, "ReadOnly");
        } else {
            return null;
        }
    }

    private BookAccessALl generateBookAccessAll(BookInfo bookInfo, UserInfo ui, String accessType) {

        BookAccessALl bookAccess = new BookAccessALl();
        BeanUtils.copyProperties(bookInfo, bookAccess);
        bookAccess.setBookName(bookInfo.getName());
        bookAccess.setAccessType(accessType);
        bookAccess.setTradingDesk(bookInfo.getTradingDesk());
        bookAccess.setUserName(ui.getFullName());
        bookAccess.setId(ui.getUserName());
        bookAccess.setDesc(ui.getDescription());
        return bookAccess;
    }

    private List<UserInfo> getALlFICCUserList(String instanceName, List<String> ficcGroupList) throws ServiceInternalException {

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        instance.setInstance(instanceName);

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        CriteriaUser criteria = new CriteriaUser();

        List<UserInfo> userInfoList = remoteUserService.getUserInfoList(instance, dateTime, criteria);
        logger.info("All user: {}", userInfoList.size());

        List<UserInfo> ficcUserList = filter(userInfoList, ficcGroupList);
        logger.info("FICC user: {}", ficcUserList.size());

        return ficcUserList;
    }

    private List<UserInfo> filter(List<UserInfo> userInfoList, List<String> ficcGroupList) {
        return userInfoList.stream().filter(user -> isIn(user.getGroupList(), ficcGroupList)).collect(Collectors.toList());
    }

    private boolean isIn(List<String> userGroupList, List<String> ficcGroupList) {
        boolean isIn = false;
        for (int i = 0; i < userGroupList.size(); i++) {
            if (ficcGroupList.contains(userGroupList.get(i))) {
                isIn = true;
            }
        }
        return isIn;
    }

    private List<BookInfo> getFICCBookList(String instanceName) throws ServiceInternalException {

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        instance.setInstance(instanceName);

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        CriteriaBook criteria = new CriteriaBook();
        criteria.setLegalEntityList(Arrays.asList("HKFH", "HKCI", "HTIFP"));
        criteria.setCompanyShortNameList(Arrays.asList("FICC", "TRES"));
        List<BookInfo> bookInfoList = remoteBookService.getBook(instance, dateTime, criteria);

        return bookInfoList;
    }

    private UserInfoVO convert(UserInfo userInfo, String groupName) {
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(userInfo, vo);
        vo.setUserGroup(groupName);
        return vo;
    }

    private List<UserInfoVO> convert(UserInfo userInfo) {
        if (CollectionUtils.isEmpty(userInfo.getGroupList())) {
            return Arrays.asList(convert(userInfo, null));
        } else {
            return userInfo.getGroupList().stream().map(t -> convert(userInfo, t)).collect(Collectors.toList());
        }
    }

    private List<UserInfoVO> convert(List<UserInfo> userInfoList) {

        List<UserInfoVO> all = new ArrayList<>();
        userInfoList.forEach(t -> all.addAll(convert(t)));
        return all;
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