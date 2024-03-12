package com.hthk.fintech.fintechservice.comparator;

import com.hthk.calypsox.model.marketdata.quote.eod.EODQuote;

import java.util.Comparator;

/**
 * @Author: Rock CHEN
 * @Date: 2024/3/11 17:14
 */
public class EODQuoteComparator implements Comparator<EODQuote> {

    @Override
    public int compare(EODQuote o1, EODQuote o2) {

        return o1.getDate().compareTo(o2.getDate());
    }
}

