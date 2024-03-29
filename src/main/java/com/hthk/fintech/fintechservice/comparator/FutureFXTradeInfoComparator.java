package com.hthk.fintech.fintechservice.comparator;

import com.hthk.calypsox.model.trade.product.FutureFXTradeInfo;

import java.time.LocalDate;
import java.util.Comparator;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/10 15:06
 */
public class FutureFXTradeInfoComparator implements Comparator<FutureFXTradeInfo> {

    @Override
    public int compare(FutureFXTradeInfo ti1, FutureFXTradeInfo ti2) {

        LocalDate settleDate1 = ti1.getSettleDate();
        LocalDate settleDate2 = ti2.getSettleDate();
        return settleDate1.compareTo(settleDate2);
    }
}
