package com.pms.rttm.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.rttm.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@RequiredArgsConstructor
@Slf4j
public class DlqWebSocketHandler implements WebSocketHandler {

    private final StatsService statsService;
    private final ObjectMapper objectMapper;
    private final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("DLQ WebSocket connection established: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("DLQ WebSocket connection closed: {}", session.getId());
    }

    @Scheduled(fixedRate = 5000)
    public void broadcastDlqStats() {
        if (sessions.isEmpty()) return;
        
        try {
            var dlqStats = statsService.getDlqStats();
            String message = objectMapper.writeValueAsString(dlqStats);
            
            sessions.removeIf(session -> {
                try {
                    session.sendMessage(new TextMessage(message));
                    return false;
                } catch (Exception e) {
                    log.warn("Failed to send DLQ message to session {}", session.getId());
                    return true;
                }
            });
        } catch (Exception e) {
            log.error("Error broadcasting DLQ stats", e);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {}

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        sessions.remove(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}