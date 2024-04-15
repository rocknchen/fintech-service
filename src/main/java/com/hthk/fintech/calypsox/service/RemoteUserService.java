package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.person.UserInfo;
import com.hthk.calypsox.model.person.UserInfoResultSet;
import com.hthk.calypsox.model.person.criteria.CriteriaUser;
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
 * @Date: 2024/4/15 17:37
 */
@Service
public class RemoteUserService extends AbstractRemoteService {

    private final static Logger logger = LoggerFactory.getLogger(RemoteUserService.class);

    public List<UserInfo> getUserInfoList(ApplicationInstance source, RequestDateTime dateTime, CriteriaUser criteria) throws ServiceInternalException {

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
        requestEntity.setType(EntityTypeEnum.USER);
        requestEntity.setSubType1("NA");
        requestEntity.setSubType2("NA");
        requestEntity.setSubType3("NA");
        requestEntity.setSubType4("NA");
        requestEntity.setSubType5("NA");

        request.setCriteria(criteria);

        String postUrl = getPostUrl(source);

        HttpResponse<UserInfoResultSet> userInfoResultSetResp = client.call(postUrl, request, UserInfoResultSet.class);
        UserInfoResultSet resultSet = JacksonUtils.jsonMapper.convertValue(userInfoResultSetResp.getData(), UserInfoResultSet.class);
        List<UserInfo> userInfoList = resultSet.getList();
        return userInfoList;
    }

}
