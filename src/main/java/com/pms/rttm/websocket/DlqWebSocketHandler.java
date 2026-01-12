package com.pms.rttm.websocket;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.rttm.dto.DlqOverview;
import com.pms.rttm.service.DlqMetricsService;
import com.pms.rttm.enums.EventStage;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DlqWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    private DlqMetricsService dlqService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                long total = dlqService.totalDlq();

                Map<String, Long> byStage = dlqService.dlqByStage().entrySet().stream()
                        .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));

                DlqOverview overview = new DlqOverview(total, byStage);
                String json = objectMapper.writeValueAsString(overview);
                session.sendMessage(new TextMessage(json));
            } catch (Exception e) {
                log.error("Error sending DLQ overview websocket message", e);
                try {
                    DlqOverview fallback = new DlqOverview(0L, new HashMap<>());
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(fallback)));
                } catch (Exception ex) {
                    log.error("Failed to send DLQ fallback", ex);
                }
            }
        }, 0, 3, TimeUnit.SECONDS);
    }
}
