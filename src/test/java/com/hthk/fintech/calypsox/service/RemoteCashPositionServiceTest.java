package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.position.CashPositionInfo;
import com.hthk.calypsox.model.position.criteria.CriteriaCashPosition;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.fintechservice.comparator.CashPositionComparator;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.hthk.calypsox.config.CalypsoStaticData.ENV_NAME_UAT;
import static com.hthk.fintech.config.FintechStaticData.LOG_WRAP;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/10 15:33
 */
public class RemoteCashPositionServiceTest {

    private final static Logger logger = LoggerFactory.getLogger(RemoteCashPositionServiceTest.class);

    private RemoteCashPositionService remoteCashPositionService = new RemoteCashPositionService();
    String outputFile;

    public RemoteCashPositionServiceTest() {

        RemoteServiceUtils.setup(remoteCashPositionService);
        outputFile = "C:/Rock/Datas/IT/DEV_Datas/tmp/cashPosition.csv";
    }

    @Test
    public void testCashPosition() throws ServiceInternalException, IOException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        instance.setInstance(ENV_NAME_UAT);

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        CriteriaCashPosition criteria = new CriteriaCashPosition();
        criteria.setBookList(Arrays.asList("CIFXDH_TEST"));

        List<CashPositionInfo> cashPositionInfoList = remoteCashPositionService.getCashPosition(instance, dateTime, criteria);
        if (!CollectionUtils.isEmpty(cashPositionInfoList)) {
            logger.info(LOG_WRAP, "cashPositionInfoList 1st", JacksonUtils.toJsonPrettyTry(cashPositionInfoList.get(0)));
            Collections.sort(cashPositionInfoList, new CashPositionComparator());
            CSVUtils.write(cashPositionInfoList, outputFile, "UTF-8", true, CashPositionInfo.class);
        } else {
            logger.info("NO position");
            new File(outputFile).delete();
        }

    }


}