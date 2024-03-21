package com.hthk.fintech.fintechservice.config;

import com.hthk.common.utils.FileUtils;
import com.hthk.fintech.config.ApplicationInfo;
import com.hthk.fintech.structure.utils.JacksonUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

import static com.hthk.fintech.config.FintechStaticData.DEFAULT_APP_INFO_FILE;

/**
 * @Author: Rock CHEN
 * @Date: 2024/3/21 14:48
 */
@Configuration
public class AppYamlConfig {

    @Bean
    public ApplicationInfo getApplicationInfo() throws IOException {

        String appInfoStr = FileUtils.readClassPathResourceAsStr(DEFAULT_APP_INFO_FILE, true);
        return JacksonUtils.readYml(appInfoStr, ApplicationInfo.class);
    }

}
