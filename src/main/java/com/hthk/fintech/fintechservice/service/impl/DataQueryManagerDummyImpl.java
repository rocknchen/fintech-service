package com.hthk.fintech.fintechservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.exception.ServiceInvalidException;
import com.hthk.fintech.exception.ServiceNotSupportedException;
import com.hthk.fintech.model.data.datacenter.query.IDataCriteria;
import com.hthk.fintech.model.web.http.HttpRequest;
import com.hthk.fintech.service.DataQueryManagerService;
import org.springframework.stereotype.Component;

/**
 * @Author: Rock CHEN
 * @Date: 2023/12/20 15:07
 */
@Component
public class DataQueryManagerDummyImpl implements DataQueryManagerService {
    @Override
    public <R> R process(HttpRequest<? extends IDataCriteria> request) throws ServiceInvalidException, JsonProcessingException, ServiceNotSupportedException, ServiceInternalException {
        return null;
    }
}
