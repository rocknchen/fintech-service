package com.hthk.fintech.fintechservice.comparator;

import com.hthk.calypsox.model.staticdata.book.BookAccessALl;

import java.util.Comparator;

/**
 * @Author: Rock CHEN
 * @Date: 2024/4/17 16:12
 */
public class BasicBookAccessALLComparator implements Comparator<BookAccessALl> {

    @Override
    public int compare(BookAccessALl b1, BookAccessALl b2) {

        String le1 = b1.getLegalEntity();
        String le2 = b2.getLegalEntity();

        String tradingDesk1 = b1.getTradingDesk();
        String tradingDesk2 = b2.getTradingDesk();

        String book1 = b1.getBookName();
        String book2 = b2.getBookName();

        String accessType1 = b1.getAccessType();
        String accessType2 = b2.getAccessType();

        String userName1 = b1.getUserName();
        String userName2 = b2.getUserName();

        String id1 = b1.getId();
        String id2 = b2.getId();

        if (!le1.equals(le2)) {
            return le1.compareTo(le2);
        }

        if (tradingDesk1 != null && tradingDesk2 != null) {
            if (!tradingDesk1.equals(tradingDesk2)) {
                return tradingDesk1.compareTo(tradingDesk2);
            }
        }

        if (!book1.equals(book2)) {
            return book1.compareTo(book2);
        }

        if (!accessType1.equals(accessType2)) {
            return accessType1.compareTo(accessType2);
        }

        if (!userName1.equals(userName2)) {
            return userName1.compareTo(userName2);
        }

        if (!id1.equals(id2)) {
            return id1.compareTo(id2);
        }

        return b1.hashCode() > b2.hashCode() ? 1 : -1;

    }
}
