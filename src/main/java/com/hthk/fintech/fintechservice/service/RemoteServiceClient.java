package com.hthk.fintech.fintechservice.service;

import com.hthk.fintech.model.web.http.HttpResponse;
import com.hthk.fintech.model.web.http.HttpServiceRequest;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/5 15:57
 */
public interface RemoteServiceClient {

    <P, C, R> HttpResponse<R> call(HttpServiceRequest<P, C> request, Class<R> clz);

}
