package com.pms.rttm.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
public abstract class AbstractRttmWsHandler extends TextWebSocketHandler {

    protected final ObjectMapper mapper;
    protected final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    protected AbstractRttmWsHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("WS connected: {}", session.getUri());
        sendInitial(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    protected void broadcast(Object payload) {
        try {
            String json = mapper.writeValueAsString(payload);
            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(json));
                }
            }
        } catch (IOException e) {
            log.error("WS broadcast failed", e);
        }
    }

    protected abstract void sendInitial(WebSocketSession session);
}
