package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.position.CashPositionInfo;
import com.hthk.calypsox.model.position.CashPositionInfoResultSet;
import com.hthk.calypsox.model.position.criteria.CriteriaCashPosition;
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
 * @Date: 2024/1/10 15:30
 */
@Service
public class RemoteCashPositionService extends AbstractRemoteService {

    private final static Logger logger = LoggerFactory.getLogger(RemoteCashPositionService.class);

    public List<CashPositionInfo> getCashPosition(ApplicationInstance source, RequestDateTime dateTime, CriteriaCashPosition criteria) throws ServiceInternalException {

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
        requestEntity.setType(EntityTypeEnum.POSITION);
        requestEntity.setSubType1("CASH");
        requestEntity.setSubType2("NA");
        requestEntity.setSubType3("NA");
        requestEntity.setSubType4("NA");
        requestEntity.setSubType5("NA");

        request.setCriteria(criteria);

        String postUrl = getPostUrl(source);

        HttpResponse<CashPositionInfoResultSet> infoResultSetResp = client.call(postUrl, request, CashPositionInfoResultSet.class);
        CashPositionInfoResultSet resultSet = JacksonUtils.jsonMapper.convertValue(infoResultSetResp.getData(), CashPositionInfoResultSet.class);
        List<CashPositionInfo> origList = resultSet.getList();
        return origList;
    }

}
