package com.suyu.websocket.controller;

import com.suyu.websocket.engine.ClientInstant;
import com.suyu.websocket.engine.IatClient;
import com.suyu.websocket.entity.ClientInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@Slf4j
public class AudioWebSocketController implements WebSocketHandler {

    private static AtomicInteger onlineCount = new AtomicInteger(0);

    private static final ConcurrentHashMap<String, ClientInfo> SN2ClientInfoMap = new ConcurrentHashMap<>(128);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sn = getSn(session);
        log.info("设备sn：{}建立websocket连接", sn);

        // 创建client
        ClientInstant clientInstant = new ClientInstant(sn);

        ClientInfo clientInfo = ClientInfo.builder().sn(sn).session(session).clientInstant(clientInstant).build();

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

        int onlineNum = subOnlineCount();
        log.info("Close a webSocket. Current connection number: " + onlineNum);
    }


    @Override
    public void handleMessage(WebSocketSession wsSession, WebSocketMessage<?> message) throws IOException {

        // 获取文本
        BinaryMessage payload = (BinaryMessage) message;
        ByteBuffer byteBuffer = payload.getPayload();
        byte[] array = byteBuffer.array();
        String sn = getSn(wsSession);

        // 获取ClientInfo
        ClientInstant clientInstant = SN2ClientInfoMap.get(sn).getClientInstant();
        IatClient client = clientInstant.getClient();

        client.post(array);

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Exception occurs on webSocket connection. disconnecting....");
        if (session.isOpen()) {
            session.close();
        }
        String sn = getSn(session);
        SN2ClientInfoMap.remove(sn);
        subOnlineCount();
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
}
