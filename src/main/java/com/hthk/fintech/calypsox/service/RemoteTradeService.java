package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.trade.TradeInfoResultSet;
import com.hthk.calypsox.model.trade.criteria.CriteriaTrade;
import com.hthk.fintech.calypsox.service.basic.AbstractRemoteService;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.model.data.datacenter.query.EntityTypeEnum;
import com.hthk.fintech.model.software.app.ApplicationInstance;
import com.hthk.fintech.model.trade.TradeInfo;
import com.hthk.fintech.model.web.http.*;
import com.hthk.fintech.structure.utils.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/5 16:39
 */
@Service
public class RemoteTradeService extends AbstractRemoteService {

    private final static Logger logger = LoggerFactory.getLogger(RemoteTradeService.class);

    /**
     * call remote service client
     *
     * @param source
     * @param dateTime
     * @param criteria
     * @return
     */
    public List<TradeInfo> getTrade(ApplicationInstance source, RequestDateTime dateTime, CriteriaTrade criteria) throws ServiceInternalException {

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
        requestEntity.setType(EntityTypeEnum.TRADE);
        requestEntity.setSubType1("NA");
        requestEntity.setSubType2("NA");
        requestEntity.setSubType3("NA");
        requestEntity.setSubType4("NA");
        requestEntity.setSubType5("NA");

        request.setCriteria(criteria);

        String postUrl = getPostUrl(source);

        HttpResponse<TradeInfoResultSet> tradeInfoResultSetResp = client.call(postUrl, request, TradeInfoResultSet.class);
        TradeInfoResultSet resultSet = JacksonUtils.jsonMapper.convertValue(tradeInfoResultSetResp.getData(), TradeInfoResultSet.class);
        List<com.hthk.calypsox.model.trade.TradeInfo> origList = resultSet.getList();
        return origList.stream().map(t -> {
            TradeInfo tradeInfo = new TradeInfo();
            BeanUtils.copyProperties(t, tradeInfo);
            return tradeInfo;
        }).collect(Collectors.toList());
    }

}
