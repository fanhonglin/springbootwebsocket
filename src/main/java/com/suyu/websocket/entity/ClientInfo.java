package com.suyu.websocket.entity;

import com.suyu.websocket.engine.ClientInstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

import java.io.Serializable;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientInfo implements Serializable {

    private static final long serialVersionUID = 8957107006902627635L;

    private String sn;

    private WebSocketSession session;

    private ClientInstant clientInstantLeft;

    private ClientInstant clientInstantRight;

}
