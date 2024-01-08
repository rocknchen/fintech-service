package com.hthk.fintech.fintechservice.service.impl;

import com.hthk.fintech.fintechservice.service.RemoteServiceClient;
import com.hthk.fintech.model.web.http.HttpResponse;
import com.hthk.fintech.model.web.http.HttpServiceRequest;
import com.hthk.fintech.serialize.HttpSerializeDefaultObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static com.hthk.fintech.config.FintechStaticData.KW_HTTP_CONTENT_TYPE_JSON_UTF8;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/5 15:57
 */
@Component
public class RemoteServiceClientImpl implements RemoteServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public <P, C, R> HttpResponse<R> call(String postUrl, HttpServiceRequest<P, C> request, Class<R> clz) {

        return callTmp(postUrl, request, clz);
    }

    private <R, C, P> HttpResponse<R> callTmp(String postUrl, HttpServiceRequest<P, C> request, Class<R> clz) {

        restTemplate = new RestTemplate();
        HttpSerializeDefaultObjectMapperFactory objectMapperFactory = new HttpSerializeDefaultObjectMapperFactory();
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter(objectMapperFactory.getObjectMapper());

        List<HttpMessageConverter<?>> messageConverters = Arrays.asList(
                new ByteArrayHttpMessageConverter(),
                new StringHttpMessageConverter(Charset.forName("utf-8")),
                new ResourceHttpMessageConverter(),
                new SourceHttpMessageConverter<>(),
                new FormHttpMessageConverter(),
                jacksonConverter
        );
        restTemplate.setMessageConverters(messageConverters);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.add(HttpHeaders.CONTENT_TYPE, KW_HTTP_CONTENT_TYPE_JSON_UTF8);

        HttpEntity<HttpServiceRequest> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<HttpResponse> result = restTemplate.postForEntity(postUrl, requestEntity, HttpResponse.class);
        return (HttpResponse<R>) result.getBody();
    }

}
