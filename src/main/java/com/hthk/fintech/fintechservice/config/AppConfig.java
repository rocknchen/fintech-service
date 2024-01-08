package com.hthk.fintech.fintechservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/8 13:07
 */
@Configuration("fintechServiceAppConfig")
public class AppConfig {

    private final static Logger logger = LoggerFactory.getLogger(AppConfig.class);

    @Value("${application.calypso.instanceList}")
    private List<String> instanceList;

    @Value("${application.calypso.serviceUrl}")
    private String serviceUrl;

    @Value("${application.calypso.serviceName}")
    private String serviceName;

    public void setInstanceList(List<String> instanceList) {
        this.instanceList = instanceList;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<String> getInstanceList() {
        return instanceList;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public String getServiceName() {
        return serviceName;
    }
}
