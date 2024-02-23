package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.pricing.TradePricingResultInfoResultSet;
import com.hthk.calypsox.model.pricing.criteria.CriteriaDateRangePricing;
import com.hthk.calypsox.model.trade.TradeInfoResultSet;
import com.hthk.fintech.calypsox.service.basic.AbstractRemoteService;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.model.data.datacenter.query.EntityTypeEnum;
import com.hthk.fintech.model.pricing.TradePricingResultInfo;
import com.hthk.fintech.model.software.app.ApplicationInstance;
import com.hthk.fintech.model.web.http.*;
import com.hthk.fintech.structure.utils.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: Rock CHEN
 * @Date: 2024/2/23 15:46
 */
@Service
public class RemotePricingService extends AbstractRemoteService {

    private final static Logger logger = LoggerFactory.getLogger(RemotePricingService.class);

    public List<TradePricingResultInfo> price(ApplicationInstance source, RequestDateTime dateTime, CriteriaDateRangePricing criteria) throws ServiceInternalException {

        HttpServiceRequest request = new HttpServiceRequest();
        IRequestAction<HttpRequestGetParams> action = new IRequestAction<>();
        action.setName(ActionTypeEnum.DATE_RANGE_PRICE);
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

        HttpResponse<TradePricingResultInfoResultSet> tradeInfoResultSetResp = client.call(postUrl, request, TradeInfoResultSet.class);
        TradePricingResultInfoResultSet resultSet = JacksonUtils.jsonMapper.convertValue(tradeInfoResultSetResp.getData(), TradePricingResultInfoResultSet.class);
        List<TradePricingResultInfo> origList = resultSet.getList();
        return origList;
    }

}
