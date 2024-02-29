package com.hthk.fintech.fintechservice.comparator;

import com.hthk.fintech.model.staticdata.BookInfo;

import java.util.Comparator;

/**
 * @Author: Rock CHEN
 * @Date: 2024/2/29 20:33
 */
public class BasicBookInfoComparator implements Comparator<BookInfo> {

    @Override
    public int compare(BookInfo b1, BookInfo b2) {

        String le1 = b1.getLegalEntity();
        String le2 = b2.getLegalEntity();

        String tradingDesk1 = b1.getTradingDesk();
        String tradingDesk2 = b2.getTradingDesk();

        String book1 = b1.getName();
        String book2 = b2.getName();

        if (!le1.equals(le2)) {
            return le1.compareTo(le2);
        }

        if (tradingDesk1 != null && tradingDesk2 != null) {
            if (!tradingDesk1.equals(tradingDesk2)) {
                return tradingDesk1.compareTo(tradingDesk2);
            }
        }

        return book1.compareTo(book2);
    }
}
