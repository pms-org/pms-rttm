package com.pms.rttm.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.rttm.dto.Alert;
import com.pms.rttm.service.AlertsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AlertsWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    private AlertsService alertsService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Alert> list = alertsService.latestByStatus("ACTIVE", 25);
                String json = objectMapper.writeValueAsString(list);
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            } catch (Exception e) {
                log.error("Error sending alerts websocket message", e);
            }
        }, 0, 3, TimeUnit.SECONDS);

        session.getAttributes().put("alertsFuture", future);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
            throws Exception {
        Object f = session.getAttributes().get("alertsFuture");
        if (f instanceof ScheduledFuture) {
            try {
                ((ScheduledFuture<?>) f).cancel(true);
            } catch (Exception e) {
                log.warn("Failed to cancel alerts future", e);
            }
        }
        super.afterConnectionClosed(session, status);
    }
}
