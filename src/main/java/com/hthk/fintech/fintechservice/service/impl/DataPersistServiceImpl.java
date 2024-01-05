package com.hthk.fintech.fintechservice.service.impl;

import com.hthk.fintech.fintechservice.service.DataPersistService;
import com.hthk.fintech.model.web.http.HttpServiceRequest;
import org.springframework.stereotype.Component;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/5 16:13
 */
@Component
public class DataPersistServiceImpl implements DataPersistService {

    @Override
    public <P, C, R> void process(HttpServiceRequest<P, C> request, Class<R> clz, String outputFile) {

    }

}
