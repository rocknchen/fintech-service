package com.hthk.fintech.fintechservice.converter.impl;

import com.hthk.calypsox.model.staticdata.future.contract.FutureInfo;
import com.hthk.calypsox.model.trade.TradeInfo;
import com.hthk.calypsox.model.trade.product.FutureFXTradeInfo;
import com.hthk.fintech.calypsox.service.RemoteTradeFutureFXService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hthk.fintech.config.FintechStaticData.LOG_DEFAULT;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/10 14:09
 */
@Component
public class FutureFXTradeInfoConverterImpl {

    private final static Logger logger = LoggerFactory.getLogger(FutureFXTradeInfoConverterImpl.class);

    public FutureFXTradeInfo process(TradeInfo tradeInfo, Map<String, List<FutureInfo>> futureInfoMap) {

        FutureInfo futureInfo = getFutureInfo(tradeInfo, futureInfoMap);

        FutureFXTradeInfo ti = new FutureFXTradeInfo();
        ti.setBook(tradeInfo.getBook());
        ti.setTradeDate(tradeInfo.getTradeDateTime().toLocalDate());
        ti.setTradeDateTime(tradeInfo.getTradeDateTime());
//        ti.setBuySell(tradeInfo);
        ti.setTradeId(tradeInfo.getTradeId());
        ti.setProductType(tradeInfo.getProductType());
        ti.setProductSubType(tradeInfo.getProductSubType());

        if (futureInfo == null) {
            logger.error(LOG_DEFAULT, tradeInfo.getTradeId(), tradeInfo.getFutureUnderlyingTickerExchange());
        }
        if (futureInfo != null) {
            ti.setTickerExchange(futureInfo.getTickerExchange());
            ti.setBbTickerExchange(futureInfo.getBbTickerExchange());
        }

        return ti;

    }

    private FutureInfo getFutureInfo(TradeInfo tradeInfo, Map<String, List<FutureInfo>> futureInfoMap) {

        String productSubType = tradeInfo.getProductSubType();
        String tickerExchange = tradeInfo.getFutureUnderlyingTickerExchange();
        List<FutureInfo> futureInfoList = futureInfoMap.get(productSubType);
        List<FutureInfo> futureInfos = futureInfoList.stream().filter(t -> tickerExchange.equals(t.getTickerExchange())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(futureInfos)) {
            return null;
        } else {
            return futureInfos.get(0);
        }
    }

}
