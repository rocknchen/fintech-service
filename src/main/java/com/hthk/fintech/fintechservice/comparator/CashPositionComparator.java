package com.hthk.fintech.fintechservice.comparator;

import com.hthk.calypsox.model.position.CashPositionInfo;

import java.util.Comparator;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/10 15:42
 */
public class CashPositionComparator implements Comparator<CashPositionInfo> {

    @Override
    public int compare(CashPositionInfo ti1, CashPositionInfo ti2) {

        String currency1 = ti1.getCurrency();
        String currency2 = ti2.getCurrency();
        return currency1.hashCode() > currency2.hashCode() ? 1 : -1;
    }
}
