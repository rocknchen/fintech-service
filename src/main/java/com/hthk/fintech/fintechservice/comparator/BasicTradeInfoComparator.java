package com.hthk.fintech.fintechservice.comparator;


import com.hthk.fintech.model.trade.TradeInfo;

import java.time.LocalDateTime;
import java.util.Comparator;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/18 10:46
 */
public class BasicTradeInfoComparator implements Comparator<TradeInfo> {

    @Override
    public int compare(TradeInfo ti1, TradeInfo ti2) {

        LocalDateTime enteredDateTime1 = ti1.getEnteredDateTime();
        LocalDateTime enteredDateTime2 = ti2.getEnteredDateTime();
        return enteredDateTime2.compareTo(enteredDateTime1);
    }
}
