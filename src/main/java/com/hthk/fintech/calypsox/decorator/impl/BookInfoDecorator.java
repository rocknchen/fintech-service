package com.hthk.fintech.calypsox.decorator.impl;

import com.hthk.calypsox.model.staticdata.BookInfo;
import com.hthk.fintech.decorator.EntityDecorator;
import com.hthk.fintech.model.common.EntityDecorateParam;
import org.springframework.stereotype.Component;

/**
 * @Author: Rock CHEN
 * @Date: 2024/2/8 12:01
 */
@Component("calypsoBookInfoDefaultDecorator")
public class BookInfoDecorator implements EntityDecorator<BookInfo, EntityDecorateParam> {

    @Override
    public BookInfo process(BookInfo entity, EntityDecorateParam param) {
        return null;
    }

}
