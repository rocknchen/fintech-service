package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.staticdata.future.contract.FutureInfo;
import com.hthk.calypsox.model.staticdata.future.contract.criteria.CriteriaFuture;
import com.hthk.calypsox.model.trade.TradeInfo;
import com.hthk.calypsox.model.trade.TradeInfoResultSet;
import com.hthk.calypsox.model.trade.criteria.CriteriaTrade;
import com.hthk.calypsox.model.trade.product.FutureFXTradeInfo;
import com.hthk.fintech.calypsox.service.basic.AbstractRemoteService;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.model.data.datacenter.query.EntityTypeEnum;
import com.hthk.fintech.model.software.app.ApplicationEnum;
import com.hthk.fintech.model.software.app.ApplicationInstance;
import com.hthk.fintech.model.web.http.*;
import com.hthk.fintech.structure.utils.JacksonUtils;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hthk.calypsox.config.CalypsoStaticData.ENV_NAME_UAT;
import static com.hthk.fintech.config.FintechStaticData.LOG_DEFAULT;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/9 21:04
 */
@Service
public class RemoteTradeFutureFXService extends AbstractRemoteService {

    private final static Logger logger = LoggerFactory.getLogger(RemoteTradeFutureFXService.class);

    Map<String, String> contractMap = new HashMap<>();

    @Autowired
    private RemoteStaticDataFutureService remoteStaticDataFutureService;

    public void setRemoteStaticDataFutureService(RemoteStaticDataFutureService remoteStaticDataFutureService) {
        this.remoteStaticDataFutureService = remoteStaticDataFutureService;
    }

    public RemoteTradeFutureFXService() {
        contractMap.put("UCA", "HKEX,USD");
        contractMap.put("USD.CNH.FX", "SGX,CNH");
    }

    public List<FutureFXTradeInfo> getTrade(ApplicationInstance source, RequestDateTime dateTime, CriteriaTrade criteria) throws ServiceInternalException {

        HttpServiceRequest request = buildRequest(source, dateTime, criteria);

        String postUrl = getPostUrl(source);

        HttpResponse<TradeInfoResultSet> tradeInfoResultSetResp = client.call(postUrl, request, TradeInfoResultSet.class);
        TradeInfoResultSet resultSet = JacksonUtils.jsonMapper.convertValue(tradeInfoResultSetResp.getData(), TradeInfoResultSet.class);
        List<TradeInfo> tradeInfoList = resultSet.getList();
        Set<String> futureContractNameSet = getFutureContract(tradeInfoList);
        logger.info(LOG_DEFAULT, "futureContractNameSet", futureContractNameSet);

        Map<String, List<FutureInfo>> futureInfoMap = getFutureInfoMap(futureContractNameSet, contractMap);
        log(futureInfoMap);

        List<FutureFXTradeInfo> futureFXTradeInfoList = convert(tradeInfoList, futureInfoMap);
        return futureFXTradeInfoList;
    }

    private Map<String, List<FutureInfo>> getFutureInfoMap(Set<String> futureContractNameSet, Map<String, String> contractMap) {

        Map<String, List<FutureInfo>> map = new HashedMap();
        futureContractNameSet.forEach(name -> {
            String contractInfoStr = contractMap.get(name);
            String exchange = contractInfoStr.split(",")[0];
            String currency = contractInfoStr.split(",")[1];
            try {
                List<FutureInfo> futureInfoList = getFutureList(exchange, name, currency, "2022-01-01");
                map.put(name, futureInfoList);
            } catch (ServiceInternalException e) {
                throw new RuntimeException(e);
            }
        });
        return map;
    }

    private List<FutureInfo> getFutureList(String exchange, String name, String currency, String expiry) throws ServiceInternalException {

        ApplicationInstance instance = new ApplicationInstance();
        instance.setName(ApplicationEnum.CALYPSO);
        instance.setInstance(ENV_NAME_UAT);

        RequestDateTime dateTime = new RequestDateTime();
        dateTime.setTimeZone("HKT");
        dateTime.setRunDateTime("2023-12-20 14:19:20");

        CriteriaFuture criteria = new CriteriaFuture();
        criteria.setExchange("HKEX");
        criteria.setName("UCA");
        criteria.setCurrency("CNH");
        criteria.setExpirationStart(LocalDate.parse(expiry, DateTimeFormatter.ISO_DATE));

        return remoteStaticDataFutureService.getFuture(instance, dateTime, criteria);
    }

    private void log(Map<String, List<FutureInfo>> futureInfoMap) {
        futureInfoMap.forEach((k, v) -> logger.info(LOG_DEFAULT, k, v.size()));
    }

    private Set<String> getFutureContract(List<TradeInfo> tradeInfoList) {
        return tradeInfoList.stream().map(t -> t.getProductSubType()).collect(Collectors.toSet());
    }

    private List<FutureFXTradeInfo> convert(List<TradeInfo> tradeInfoList, Map<String, List<FutureInfo>> futureInfoMap) {
        return tradeInfoList.stream().map(t -> convert(t, futureInfoMap)).collect(Collectors.toList());
    }

    private FutureFXTradeInfo convert(TradeInfo tradeInfo, Map<String, List<FutureInfo>> futureInfoMap) {

        FutureFXTradeInfo ti = new FutureFXTradeInfo();
        ti.setBook(tradeInfo.getBook());
        ti.setTradeDate(tradeInfo.getTradeDateTime().toLocalDate());
        ti.setTradeDateTime(tradeInfo.getTradeDateTime());
//        ti.setBuySell(tradeInfo);
        ti.setProductType(tradeInfo.getProductType());
        ti.setProductSubType(tradeInfo.getProductSubType());

        return ti;

    }

    private HttpServiceRequest buildRequest(ApplicationInstance source, RequestDateTime dateTime, CriteriaTrade criteria) {

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
        return request;
    }

}

