package com.suyu.websocket.controller;

import com.alibaba.fastjson.JSON;
import com.suyu.websocket.util.send.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@Slf4j
public class TextWebSocketController implements WebSocketHandler {

    private static AtomicInteger onlineCount = new AtomicInteger(0);

    private static final ConcurrentHashMap<String, WebSocketSession> SN2SESSIONMAP = new ConcurrentHashMap<>(256);

    private final Logger LOGGER = LoggerFactory.getLogger(TextWebSocketController.class);

    @Resource
    private ApiHelper apiHelper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        URI uri = session.getUri();
        String query = uri.getQuery();
        if (!StringUtils.isEmpty(query)) {

            String sn = getSn(query);


            SN2SESSIONMAP.put(sn, session);
            // 开启录音
            orderRealTimeTransfer(Arrays.asList(MD5Util.getDeviceNameBySN(sn)), 1);
            log.info("文本设备sn:{}已经建立连接", sn);
        }
    }

    private String getSn(String query) {
        String sn = query.split("=")[1];
        return sn;
    }

    public void orderRealTimeTransfer(List<String> deviceNames, int status) {
        Map<String, Object> map = new HashMap<>();
        map.put("opt", 62009);
        map.put("optnum", OptNumUtils.getOptNum());
        map.put("block", 1);
        map.put("ctrl", status);
//        map.put("url", "ws://222.212.89.53:58080/iot-websocket/socketServer");
        map.put("url", "ws://10.40.119.20:8086/socketServer");
        gotoDeviceCommonOrder(deviceNames, JSON.toJSONString(map));
    }

    public void gotoDeviceCommonOrder(List<String> deviceNames, String content) {

        for (String deviceName : deviceNames) {
            HashMap<String, Object> params = DeviceCodeUtils.baseParameter(deviceName, content);
            log.info("下发的指令内容：" + content);
            // 发送指令
            try {
                IoTResponse<Map> response = apiHelper.doPost(IoTConstants.API_COMMAND_PUSH, params, Map.class);
                log.info("下发指令完成：" + JSON.toJSONString(response));
            } catch (Exception e) {
                log.error("指令下发失败：" + e.getMessage());
            }
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

        URI uri = session.getUri();
        String query = uri.getQuery();
        if (!StringUtils.isEmpty(query)) {
            String sn = getSn(query);
            SN2SESSIONMAP.remove(sn);
            // 开启录音
            orderRealTimeTransfer(Arrays.asList(MD5Util.getDeviceNameBySN(sn)), 0);
            log.info("文本设备sn:{}已经断开连接", sn);
        }
    }

    @Override
    public void handleMessage(WebSocketSession wsSession, WebSocketMessage<?> message) throws Exception {
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

        URI uri = session.getUri();
        String query = uri.getQuery();

        LOGGER.error("Exception occurs on webSocket connection. disconnecting....");
        if (session.isOpen()) {
            session.close();
        }

        if (!StringUtils.isEmpty(query)) {
            String sn = getSn(query);
            SN2SESSIONMAP.remove(sn);
            // 开启录音
            orderRealTimeTransfer(Arrays.asList(MD5Util.getDeviceNameBySN(sn)), 0);
            log.error("文本设备sn:{}已经断开连接", sn);
        }
    }

    /*
     * 是否支持消息拆分发送：如果接收的数据量比较大，最好打开(true), 否则可能会导致接收失败。
     * 如果出现WebSocket连接接收一次数据后就自动断开，应检查是否是这里的问题。
     */
    @Override
    public boolean supportsPartialMessages() {
        return true;
    }


    public static int subOnlineCount() {
        return onlineCount.decrementAndGet();
    }


    public void sendMessage(String msg, String sn) throws IOException {
        WebSocketSession webSocketSession = SN2SESSIONMAP.get(sn);
        if (Objects.isNull(webSocketSession)) {
            log.error("文本发送没有建立websocket连接");
            return;
        }

        TextMessage textMessage = new TextMessage(msg);
        if (Objects.isNull(webSocketSession)) {
            return;
        }
        webSocketSession.sendMessage(textMessage);
    }
}
