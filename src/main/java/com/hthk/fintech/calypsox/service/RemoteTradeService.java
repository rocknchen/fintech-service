package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.trade.criteria.CriteriaTrade;
import com.hthk.fintech.model.software.app.ApplicationInstance;
import com.hthk.fintech.model.trade.TradeInfo;
import com.hthk.fintech.model.web.http.RequestDateTime;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/5 16:39
 */
@Service
public class RemoteTradeService {

    /**
     * call remote service client
     * @param source
     * @param dateTime
     * @param criteria
     * @return
     */
    public List<TradeInfo> getTrade(ApplicationInstance source, RequestDateTime dateTime, CriteriaTrade criteria) {
        return null;
    }

}
