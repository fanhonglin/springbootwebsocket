package com.suyu.websocket.util.send;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Joey Fan
 * @Description 设备指令码
 * @Date 15:33 2022/5/26
 * @Param
 * @return
 **/
public class DeviceCodeUtils {

    /**
     * @return java.util.HashMap<java.lang.String, java.lang.Object>
     * @Author Joey Fan
     * @Description 参数
     * @Date 15:28 2022/5/26
     * @Param [deviceName, content]
     **/
    public static HashMap<String, Object> baseParameter(String deviceName,
                                                        String content) {

        // 参数
        HashMap<String, Object> parameterMap = new HashMap<>(8);

        parameterMap.put("productKey", IoTConstants.PRODUCT_KEY);
        parameterMap.put("deviceName", deviceName);
        parameterMap.put("serviceId", IoTConstants.SERVICE_ID);
        parameterMap.put("async", false);

        // 指令码
        Map<String, Object> codeMap = new HashMap(4);
        codeMap.put("data", content);
        parameterMap.put("params", codeMap);

        return parameterMap;
    }
}
