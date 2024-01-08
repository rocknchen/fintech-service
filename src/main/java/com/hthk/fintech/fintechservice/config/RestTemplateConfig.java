package com.hthk.fintech.fintechservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/8 13:18
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(simpleFactory());
        restTemplate.setRequestFactory(simpleFactory());
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestFactory simpleFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 响应超时时间20s
        factory.setReadTimeout(20 * 1000);
        // 连接超时10s
        factory.setConnectTimeout(10 * 1000);
        return factory;
    }

}
