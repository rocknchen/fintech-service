package com.hthk.fintech.model.web.http;

import com.hthk.fintech.model.common.JsonSerializeConfig;

/**
 * @Author: Rock CHEN
 * @Date: 2024/1/2 11:09
 */
@JsonSerializeConfig(target = HttpRequestGetParams.class
        , ignoreFunctions = {"getApplicationSource"})
public class HttpRequestGetParamsConfig {

}
