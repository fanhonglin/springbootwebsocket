package com.suyu.websocket.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@Slf4j
public class TextWebSocketController implements WebSocketHandler {

    private static AtomicInteger onlineCount = new AtomicInteger(0);

    private static final ConcurrentHashMap<String, WebSocketSession> SN2SESSIONMAP = new ConcurrentHashMap<>(256);

    private final Logger LOGGER = LoggerFactory.getLogger(TextWebSocketController.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        URI uri = session.getUri();
        String query = uri.getQuery();
        if (!StringUtils.isEmpty(query)) {
            String sn = query.split("=")[1];
            SN2SESSIONMAP.put(sn, session);
            log.info("文本设备sn:{}已经建立连接", sn);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        SN2SESSIONMAP.remove(session);
        int onlineNum = subOnlineCount();
        LOGGER.info("Close a webSocket. Current connection number: " + onlineNum);
    }

    @Override
    public void handleMessage(WebSocketSession wsSession, WebSocketMessage<?> message) throws Exception {
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        LOGGER.error("Exception occurs on webSocket connection. disconnecting....");
        if (session.isOpen()) {
            session.close();
        }
        SN2SESSIONMAP.remove(session);
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
