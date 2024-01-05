package com.hthk.fintech.fintechservice.service;

import com.hthk.fintech.model.web.http.HttpServiceRequest;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/5 16:08
 */
public interface DataPersistService {

    /**
     * call remote Trade Service
     *
     * @param request
     * @param clz
     * @param outputFile
     * @param <P>
     * @param <C>
     * @param <R>
     */
    <P, C, R> void process(HttpServiceRequest<P, C> request, Class<R> clz, String outputFile);

}
