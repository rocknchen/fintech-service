package com.hthk.fintech.calypsox.service.basic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hthk.fintech.exception.ServiceInternalException;
import com.hthk.fintech.fintechservice.config.AppConfig;
import com.hthk.fintech.fintechservice.service.impl.RemoteServiceClientImpl;
import com.hthk.fintech.model.software.app.ApplicationInstance;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/9 20:29
 */
public abstract class AbstractRemoteService {

    protected RemoteServiceClientImpl client = new RemoteServiceClientImpl();

    @Autowired
    protected AppConfig fsAppConfig;

    protected ObjectMapper objectMapper = new ObjectMapper();

    public void setFsAppConfig(AppConfig fsAppConfig) {
        this.fsAppConfig = fsAppConfig;
    }

    protected String getPostUrl(ApplicationInstance source) throws ServiceInternalException {

        String instance = source.getInstance();
        int servicePort = getServicePort(fsAppConfig, instance);
        return fsAppConfig.getServiceUrl() + ":" + servicePort + "/" + fsAppConfig.getServiceName();

    }

    protected int getServicePort(AppConfig fsAppConfig, String instance) throws ServiceInternalException {
        String serviceStr = fsAppConfig.getInstanceList().stream().filter(t -> t.contains(instance)).findFirst().orElseThrow(
                () -> new ServiceInternalException("not support instance: " + instance));
        return Integer.valueOf(serviceStr.split(";")[1]);
    }

}
