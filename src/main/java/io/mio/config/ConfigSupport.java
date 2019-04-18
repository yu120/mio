package io.mio.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.HashMap;
import java.util.Map;

public class ConfigSupport {

    @SuppressWarnings("unchecked")
    public static Map<String, String> buildParameters(String prefixKey, String parametersKey, Object object) {
        Map<String, String> parameters = new HashMap<>();

        Map<String, Object> dataMap = null;//JSON.parseObject(JSON.toJSONString(object), new TypeReference<>());
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            if (entry.getKey().equals(parametersKey)) {
                continue;
            }
            parameters.put(prefixKey + "." + entry.getKey(), String.valueOf(entry.getValue()));
        }

        Map<String, Object> parameterMap = (Map<String, Object>) dataMap.get(parametersKey);
        if (parameterMap != null) {
            for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
                parameters.put(prefixKey + "." + parametersKey + "." + entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        return parameters;
    }

}
