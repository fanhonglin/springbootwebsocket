package com.suyu.websocket.util.send;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author lazycece
 */
@SuppressWarnings("unchecked")
@Component
public class ApiHelper {

    @Value("${iot.platform:linkapi.xfyun.cn}")
    private String HOST;

    private static final String API_KEY = IoTConstants.API_KEY;
    private static final String API_SECRET = IoTConstants.API_SECRET;
    private IoTRequest ioTRequest;

    public ApiHelper() {
        this.ioTRequest = new IoTRequest();
    }

    public <T> IoTResponse<T> doGet(String path, Map<String, String> params, Class<T> clazz) throws Exception {
        if (params != null) {
            path += getQueryString(params);
        }
        String resp = ioTRequest.get(HOST, path, API_KEY, API_SECRET);
        return getResponse(resp, clazz);
    }

    public <T> IoTResponse<T> doPost(String path, Map<String, Object> params, Class<T> clazz) throws Exception {
        String resp = ioTRequest.post(HOST, path, API_KEY, API_SECRET, new JSONObject(params));
        return getResponse(resp, clazz);
    }

    public <T> IoTResponse<T> doPost(String path, JSON params, Class<T> clazz) throws Exception {
        String resp = ioTRequest.post(HOST, path, API_KEY, API_SECRET, params.toJSONString());
        return getResponse(resp, clazz);
    }

    public <T> IoTResponse<T> doPut(String path, Map<String, Object> params, Class<T> clazz) throws Exception {
        String resp = ioTRequest.put(HOST, path, API_KEY, API_SECRET, new JSONObject(params));
        return getResponse(resp, clazz);
    }

    private static String getQueryString(Map<String, String> map) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                builder.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        return builder.substring(1);
    }

    private <T> IoTResponse<T> getResponse(String resp, Class<T> clazz) {
        IoTResponse<T> ioTResponse = JSON.parseObject(resp, IoTResponse.class);
        if (ioTResponse.getData() != null) {
            ioTResponse.setData(JSON.parseObject(JSON.toJSONString(ioTResponse.getData()), clazz));
        }
        return ioTResponse;
    }
}
