package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.staticdata.future.contract.FutureInfo;
import com.hthk.calypsox.model.staticdata.future.contract.FutureInfoResultSet;
import com.hthk.calypsox.model.staticdata.future.contract.criteria.CriteriaFuture;
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
 * @Date: 2024/1/9 20:26
 */
@Service
public class RemoteStaticDataFutureService extends AbstractRemoteService {

    private final static Logger logger = LoggerFactory.getLogger(RemoteStaticDataFutureService.class);

    public List<FutureInfo> getFuture(ApplicationInstance source, RequestDateTime dateTime, CriteriaFuture criteria) throws ServiceInternalException {

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
        requestEntity.setSubType1("FUTURE");
        requestEntity.setSubType2("NA");
        requestEntity.setSubType3("NA");
        requestEntity.setSubType4("NA");
        requestEntity.setSubType5("NA");

        request.setCriteria(criteria);

        String postUrl = getPostUrl(source);

        HttpResponse<FutureInfoResultSet> infoResultSetResp = client.call(postUrl, request, FutureInfoResultSet.class);
        FutureInfoResultSet resultSet = JacksonUtils.jsonMapper.convertValue(infoResultSetResp.getData(), FutureInfoResultSet.class);
        List<FutureInfo> origList = resultSet.getList();
        return origList;
    }

}
