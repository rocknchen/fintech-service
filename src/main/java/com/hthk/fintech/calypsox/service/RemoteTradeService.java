package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.trade.criteria.CriteriaTrade;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.fintechservice.config.AppConfig;
import com.hthk.fintech.fintechservice.service.impl.RemoteServiceClientImpl;
import com.hthk.fintech.model.data.datacenter.query.EntityTypeEnum;
import com.hthk.fintech.model.software.app.ApplicationInstance;
import com.hthk.fintech.model.trade.TradeInfo;
import com.hthk.fintech.model.web.http.*;
import com.hthk.fintech.structure.utils.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.hthk.fintech.config.FintechStaticData.LOG_WRAP;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/5 16:39
 */
@Service
public class RemoteTradeService {

    private final static Logger logger = LoggerFactory.getLogger(RemoteTradeService.class);

    private RemoteServiceClientImpl client = new RemoteServiceClientImpl();

    @Autowired
    private AppConfig fsAppConfig;

    public void setFsAppConfig(AppConfig fsAppConfig) {
        this.fsAppConfig = fsAppConfig;
    }

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

        logger.info(LOG_WRAP, "response", JacksonUtils.toJsonPrettyTry(client.call(postUrl, request, HttpResponse.class)));

        return null;
    }

    private String getPostUrl(ApplicationInstance source) throws ServiceInternalException {

        String instance = source.getInstance();
        int servicePort = getServicePort(fsAppConfig, instance);
        return fsAppConfig.getServiceUrl() + ":" + servicePort + "/" + fsAppConfig.getServiceName();

    }

    private int getServicePort(AppConfig fsAppConfig, String instance) throws ServiceInternalException {
        String serviceStr = fsAppConfig.getInstanceList().stream().filter(t -> t.contains(instance)).findFirst().orElseThrow(() -> new ServiceInternalException());
        return Integer.valueOf(serviceStr.split(";")[1]);
    }

}
