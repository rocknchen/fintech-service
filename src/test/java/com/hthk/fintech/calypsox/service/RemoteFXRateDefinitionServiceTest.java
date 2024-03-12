package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.staticdata.fxrate.definition.FXRateResetDefinitionInfo;
import com.hthk.calypsox.model.staticdata.fxrate.definition.criteria.CriteriaFXRateDef;
import com.hthk.calypsox.model.trade.criteria.CriteriaTrade;
import com.hthk.calypsox.model.trade.product.FutureFXTradeInfo;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.fintechservice.comparator.FXRateResetDefinitionInfoComparator;
import com.hthk.fintech.fintechservice.comparator.FutureFXTradeInfoComparator;
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
 * @Date: 2024/3/7 9:29
 */
public class RemoteFXRateDefinitionServiceTest {

    private final static Logger logger = LoggerFactory.getLogger(RemoteFXRateDefinitionServiceTest.class);

    private RemoteFXRateDefinitionService remoteFXRateDefinitionService = new RemoteFXRateDefinitionService();

    public RemoteFXRateDefinitionServiceTest() {
        RemoteServiceUtils.setup(remoteFXRateDefinitionService);
    }

    @Test
    public void testGetFXRateDef() throws ServiceInternalException, IOException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        String outputFile = "C:/Rock/Datas/IT/DEV_Datas/tmp/FXRateDefinition.csv";

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        instance.setInstance(ENV_NAME_UAT);

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        CriteriaFXRateDef criteria = new CriteriaFXRateDef();
        criteria.setPrimCurrency("XAU");

        List<FXRateResetDefinitionInfo> fxRateResetDefList = remoteFXRateDefinitionService.getFXRateResetDefinition(instance, dateTime, criteria);
        if (!CollectionUtils.isEmpty(fxRateResetDefList)) {
            logger.info(LOG_WRAP, "fxRateResetDef 1st", JacksonUtils.toJsonPrettyTry(fxRateResetDefList.get(0)));

            Collections.sort(fxRateResetDefList, new FXRateResetDefinitionInfoComparator());
            CSVUtils.write(fxRateResetDefList, outputFile, "UTF-8", true, FutureFXTradeInfo.class);
        } else {
            new File(outputFile).deleteOnExit();
        }

    }

}