package com.hthk.fintech.fintechservice.converter.impl;

import com.hthk.calypsox.model.staticdata.future.contract.FutureInfo;
import com.hthk.calypsox.model.trade.TradeInfo;
import com.hthk.calypsox.model.trade.product.FutureFXTradeInfo;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
        BeanUtils.copyProperties(tradeInfo, ti);

        ti.setBook(tradeInfo.getBook());
        ti.setCounterParty(tradeInfo.getCounterParty());
        ti.setTradeDate(tradeInfo.getTradeDateTime().toLocalDate());
        ti.setTradeDateTime(tradeInfo.getTradeDateTime());

        ti.setBuySell(tradeInfo.getBuySell());
        ti.setQuantity(new BigDecimal(tradeInfo.getQuantity()));

        ti.setPrice(tradeInfo.getPrice());

        ti.setTradeId(tradeInfo.getTradeId());
        ti.setProductType(tradeInfo.getProductType());
        ti.setProductSubType(tradeInfo.getProductSubType());

        ti.setSettleDate(tradeInfo.getSettlementDate());

        ti.setQuoteName(futureInfo.getQuoteName());

        ti.setFirstTradeDate(futureInfo.getFirstTradeDate());
        ti.setFirstDeliveryDate(futureInfo.getFirstDeliveryDate());
        ti.setExpirationDate(futureInfo.getExpirationDate());

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
