package com.hthk.fintech.utils;

import com.hthk.fintech.calypsox.service.basic.AbstractRemoteService;
import com.hthk.fintech.fintechservice.config.AppConfig;

import java.util.Arrays;

import static com.hthk.calypsox.config.CalypsoStaticData.ENV_NAME_FIRE_FIGHT;
import static com.hthk.calypsox.config.CalypsoStaticData.ENV_NAME_UAT;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/9 20:40
 */
public class RemoteServiceUtils {

    public static <T extends AbstractRemoteService> void setup(T service) {
        AppConfig appConfig = new AppConfig();
        appConfig.setServiceName("services");
        appConfig.setServiceUrl("http://127.0.0.1");
        appConfig.setInstanceList(Arrays.asList(ENV_NAME_UAT + ";30087", ENV_NAME_FIRE_FIGHT + ";30129"));
        service.setFsAppConfig(appConfig);
    }

}
