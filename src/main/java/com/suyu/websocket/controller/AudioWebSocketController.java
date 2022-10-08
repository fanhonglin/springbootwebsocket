package com.suyu.websocket.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class AudioWebSocketController implements WebSocketHandler {

    private static AtomicInteger onlineCount = new AtomicInteger(0);

    private static final ArrayList<WebSocketSession> sessions = new ArrayList<>();

    private final Logger LOGGER = LoggerFactory.getLogger(AudioWebSocketController.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);

        HttpHeaders handshakeHeaders = session.getHandshakeHeaders();
        List<String> list = handshakeHeaders.get("x-session-id");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        int onlineNum = subOnlineCount();
        LOGGER.info("Close a webSocket. Current connection number: " + onlineNum);
    }

    @Override
    public void handleMessage(WebSocketSession wsSession, WebSocketMessage<?> message) throws Exception {
        BinaryMessage payload = (BinaryMessage) message;


        ByteBuffer byteBuffer = payload.getPayload();

        byte[] array = byteBuffer.array();
        LOGGER.info("Receive a message from client: " + array);

        HttpHeaders handshakeHeaders = wsSession.getHandshakeHeaders();
        List<String> list = handshakeHeaders.get("x-session-id");
        System.out.println("sessionId是:" + list.get(0));

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        LOGGER.error("Exception occurs on webSocket connection. disconnecting....");
        if (session.isOpen()) {
            session.close();
        }
        sessions.remove(session);
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


    public static int getOnlineCount() {
        return onlineCount.get();
    }

    public static int addOnlineCount() {
        return onlineCount.incrementAndGet();
    }

    public static int subOnlineCount() {
        return onlineCount.decrementAndGet();
    }

}
