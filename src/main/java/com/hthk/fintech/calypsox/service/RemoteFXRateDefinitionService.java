package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.staticdata.fxrate.definition.FXRateResetDefResultSet;
import com.hthk.calypsox.model.staticdata.fxrate.definition.FXRateResetDefinitionInfo;
import com.hthk.calypsox.model.staticdata.fxrate.definition.criteria.CriteriaFXRateDef;
import com.hthk.fintech.calypsox.service.basic.AbstractRemoteService;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.model.data.datacenter.query.EntityTypeEnum;
import com.hthk.fintech.model.software.app.ApplicationInstance;
import com.hthk.fintech.model.web.http.*;
import com.hthk.fintech.structure.utils.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: Rock CHEN
 * @Date: 2024/3/7 9:21
 */
@Service
public class RemoteFXRateDefinitionService extends AbstractRemoteService {

    private final static Logger logger = LoggerFactory.getLogger(RemoteFXRateDefinitionService.class);

    public List<FXRateResetDefinitionInfo> getFXRateResetDefinition(ApplicationInstance source, RequestDateTime dateTime, CriteriaFXRateDef criteria) throws ServiceInternalException {

        HttpServiceRequest request = new HttpServiceRequest();
        IRequestAction<HttpRequestGetParams> action = new IRequestAction<>();
        action.setName(ActionTypeEnum.GET);
        HttpRequestGetParams params = new HttpRequestGetParams();
        params.setSource(source);
        action.setParams(params);
        request.setAction(action);

        request.setDateTime(dateTime);

        RequestEntity requestEntity = new RequestEntity();
        request.setEntity(requestEntity);
        requestEntity.setType(EntityTypeEnum.STATIC_DATA);
        requestEntity.setSubType1("FX_RATE_RESET");
        requestEntity.setSubType2("NA");
        requestEntity.setSubType3("NA");
        requestEntity.setSubType4("NA");
        requestEntity.setSubType5("NA");

        request.setCriteria(criteria);
        criteria.setPrimCurrency("XAU");

        String postUrl = getPostUrl(source);

        HttpResponse<FXRateResetDefinitionInfo> resultSetResp = client.call(postUrl, request, FXRateResetDefinitionInfo.class);
        FXRateResetDefResultSet resultSet = JacksonUtils.jsonMapper.convertValue(resultSetResp.getData(), FXRateResetDefResultSet.class);
        List<FXRateResetDefinitionInfo> origList = resultSet.getList();
        return origList;
    }

}
