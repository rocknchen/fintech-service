package com.hthk.fintech.calypsox.service;

import com.hthk.calypsox.model.staticdata.BookInfoResultSet;
import com.hthk.calypsox.model.staticdata.book.criteria.CriteriaBook;
import com.hthk.fintech.calypsox.service.basic.AbstractRemoteService;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.model.data.datacenter.query.EntityTypeEnum;
import com.hthk.fintech.model.software.app.ApplicationInstance;
import com.hthk.fintech.model.staticdata.BookInfo;
import com.hthk.fintech.model.web.http.*;
import com.hthk.fintech.structure.utils.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: Rock CHEN
 * @Date: 2024/2/20 15:41
 */
@Service
public class RemoteBookService extends AbstractRemoteService {

    private final static Logger logger = LoggerFactory.getLogger(RemoteBookService.class);

    /**
     * call remote service client
     *
     * @param source
     * @param dateTime
     * @param criteria
     * @return
     */
    public List<BookInfo> getBook(ApplicationInstance source, RequestDateTime dateTime, CriteriaBook criteria) throws ServiceInternalException {

        HttpServiceRequest request = new HttpServiceRequest();
        IRequestAction<HttpRequestGetParams> action = new IRequestAction<>();
        action.setName(ActionTypeEnum.GET);
        HttpRequestGetParams params = new HttpRequestGetParams();
        params.setSource(source);
        action.setParams(params);
        request.setAction(action);

        request.setDateTime(dateTime);

        RequestEntity requestEntity = new RequestEntity();
        request.setEntity(requestEntity);
        requestEntity.setType(EntityTypeEnum.STATIC_DATA);
        requestEntity.setSubType1("BOOK");
        requestEntity.setSubType2("NA");
        requestEntity.setSubType3("NA");
        requestEntity.setSubType4("NA");
        requestEntity.setSubType5("NA");

        request.setCriteria(criteria);

        String postUrl = getPostUrl(source);

        HttpResponse<BookInfoResultSet> resultSetResp = client.call(postUrl, request, BookInfoResultSet.class);
        BookInfoResultSet resultSet = JacksonUtils.jsonMapper.convertValue(resultSetResp.getData(), BookInfoResultSet.class);
        List<BookInfo> origList = resultSet.getList();
        return origList;
    }

}
