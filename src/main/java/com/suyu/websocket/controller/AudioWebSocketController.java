package com.suyu.websocket.controller;

import com.suyu.websocket.engine.ClientInstant;
import com.suyu.websocket.engine.IatClient;
import com.suyu.websocket.entity.ClientInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@Slf4j
public class AudioWebSocketController implements WebSocketHandler {

    private static final ConcurrentHashMap<String, ClientInfo> SN2ClientInfoMap = new ConcurrentHashMap<>(128);

    @Value("${engineUrl:10.40.7.30:9177}")
    private String engineUrl;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sn = getSn(session);
        log.info("设备sn：{}建立websocket连接", sn);

        cacheSession(sn,session);
    }

    private void cacheSession(String sn, WebSocketSession session) {
        // 创建client
        ClientInstant clientInstantLeft = new ClientInstant(sn, 0, engineUrl);
        ClientInstant clientInstantRight = new ClientInstant(sn, 1, engineUrl);

        ClientInfo clientInfo = ClientInfo.builder()
                .sn(sn)
                .session(session)
                .clientInstantLeft(clientInstantLeft)
                .clientInstantRight(clientInstantRight)
                .build();

        SN2ClientInfoMap.put(sn, clientInfo);
    }

    private String getSn(WebSocketSession session) {
        HttpHeaders handshakeHeaders = session.getHandshakeHeaders();
        List<String> headerSn = handshakeHeaders.get("x-sn");
        String sn = null;
        if (!CollectionUtils.isEmpty(headerSn)) {
            sn = headerSn.get(0);
            return sn;
        }
        return sn;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sn = getSn(session);
        log.info("设备sn：{}断开websocket连接", sn);

        SN2ClientInfoMap.remove(sn);
    }


    @Override
    public void handleMessage(WebSocketSession wsSession, WebSocketMessage<?> message) {

        String sn = getSn(wsSession);

//        log.info("sn为：{}的设备传递了数据", sn);

        ClientInfo clientInfo = SN2ClientInfoMap.get(sn);
        if (Objects.isNull(clientInfo)) {
            log.error("sn为：{}的设备没有建立连接,传递了数据", sn);
            return;
        }

        // 获取ClientInfo ，左声道
        ClientInstant clientInstantLeft = clientInfo.getClientInstantLeft();
        if (Objects.isNull(clientInstantLeft)) {
            log.error("sn为：{}的设备没有建立连接", sn);
            return;
        }

        // 右声道
        ClientInstant clientInstantRight = clientInfo.getClientInstantRight();

        // 获取文本
        BinaryMessage payload = (BinaryMessage) message;
        ByteBuffer byteBuffer = payload.getPayload();
        byte[] array = byteBuffer.array();

        int halfSize = array.length / 2;

        byte[] left = new byte[halfSize];
        byte[] right = new byte[halfSize];

        int j = 0;
        for (int i = 0; i < array.length; i = i + 4) {

            // 左声道第一位
            left[j] = array[i];

            // 右声道第一位
            right[j] = array[i + 2];

            j++;
            // 左声道第二位
            left[j] = array[i + 1];

            // 右声道第而位
            right[j] = array[i + 3];

            j++;
        }

        try {
            // 左声道
            IatClient clientLeft = clientInstantLeft.getClient();
            clientLeft.post(left);

            // 右声道
            IatClient clientRight = clientInstantRight.getClient();
            clientRight.post(right);
        } catch (Exception exception) {
            // 如果call was cancelled，那么新建连接
            log.error("call was cancelled");
            cacheSession(sn,wsSession);
        }
//
//        // 原始音频
//        ClientInstant clientInstant = SN2ClientInfoMap.get(sn).getClientInstantRight();
//        IatClient client = clientInstant.getClient();
//        client.post(array);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Exception occurs on webSocket connection. disconnecting....");
        if (session.isOpen()) {
            session.close();
        }
        String sn = getSn(session);
        SN2ClientInfoMap.remove(sn);
    }

    /*
     * 是否支持消息拆分发送：如果接收的数据量比较大，最好打开(true), 否则可能会导致接收失败。
     * 如果出现WebSocket连接接收一次数据后就自动断开，应检查是否是这里的问题。
     */
    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

}
