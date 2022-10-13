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
import java.util.concurrent.atomic.AtomicInteger;

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

        // 创建client
        ClientInstant clientInstantLeft = new ClientInstant(sn, 0,engineUrl);
        ClientInstant clientInstantRight = new ClientInstant(sn, 1,engineUrl);

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

        // 获取ClientInfo ，左声道
        ClientInstant clientInstantLeft = SN2ClientInfoMap.get(sn).getClientInstantLeft();
        if (Objects.isNull(clientInstantLeft)) {
            log.error("sn为：{}的设备没有建立连接", sn);
            return;
        }

        // 右声道
        ClientInstant clientInstant = SN2ClientInfoMap.get(sn).getClientInstantRight();

        // 获取文本
        BinaryMessage payload = (BinaryMessage) message;
        ByteBuffer byteBuffer = payload.getPayload();
        byte[] array = byteBuffer.array();

        int halfSize = array.length / 2;

        byte[] left = new byte[halfSize];
        byte[] right = new byte[halfSize];

        int j = 0;
        for (int i = 0; i < array.length; i = i + 2) {
            left[j] = array[i];
            right[j] = array[i + 1];
            j++;
        }

        IatClient clientLeft = clientInstantLeft.getClient();
        clientLeft.post(left);

        IatClient clientLeftRight = clientInstant.getClient();
        clientLeftRight.post(right);
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
