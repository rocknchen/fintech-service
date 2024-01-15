package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.marketdata.quote.eod.EODQuote;
import com.hthk.calypsox.model.quote.CriteriaEODQuote;
import com.hthk.calypsox.model.quote.EODQuoteResultSet;
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
 * @Date: 2024/1/15 17:59
 */
@Service
public class RemoteEODQuoteService extends AbstractRemoteService {

    private final static Logger logger = LoggerFactory.getLogger(RemoteEODQuoteService.class);

    /**
     * call remote service client
     *
     * @param source
     * @param dateTime
     * @param criteria
     * @return
     */
    public List<EODQuote> getQuote(ApplicationInstance source, RequestDateTime dateTime, CriteriaEODQuote criteria) throws ServiceInternalException {

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
        requestEntity.setType(EntityTypeEnum.EOD_QUOTE);
        requestEntity.setSubType1("NA");
        requestEntity.setSubType2("NA");
        requestEntity.setSubType3("NA");
        requestEntity.setSubType4("NA");
        requestEntity.setSubType5("NA");

        request.setCriteria(criteria);

        String postUrl = getPostUrl(source);

        HttpResponse<EODQuoteResultSet> infoResultSetResp = client.call(postUrl, request, EODQuoteResultSet.class);
        EODQuoteResultSet resultSet = JacksonUtils.jsonMapper.convertValue(infoResultSetResp.getData(), EODQuoteResultSet.class);
        List<EODQuote> origList = resultSet.getEodQuoteList();
        return origList;
    }

}