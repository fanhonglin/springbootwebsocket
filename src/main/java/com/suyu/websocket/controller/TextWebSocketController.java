package com.suyu.websocket.controller;

import com.suyu.websocket.entity.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class TextWebSocketController implements WebSocketHandler {

    private static AtomicInteger onlineCount = new AtomicInteger(0);

    private static final ArrayList<WebSocketSession> sessions = new ArrayList<>();

    private static CopyOnWriteArraySet<Client> socketServers = new CopyOnWriteArraySet<>();

    private final Logger LOGGER = LoggerFactory.getLogger(TextWebSocketController.class);

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


    /**
     * 消息推送
     *
     * @param msg
     * @throws IOException
     */
    public void sendMessage(String msg) throws IOException {
        TextMessage textMessage = new TextMessage(msg);
        sessions.get(0).sendMessage(textMessage);
    }
}
