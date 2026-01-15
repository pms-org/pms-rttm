package com.pms.rttm.websocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.rttm.service.PipelineDepthService;

import lombok.extern.slf4j.Slf4j;

import com.pms.rttm.dto.PipelineStageMetrics;
import com.pms.rttm.enums.EventStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Component
public class PipelineWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    private PipelineDepthService pipelineService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Map<String, Object>> pipeline = generatePipelineData();
                String json = objectMapper.writeValueAsString(pipeline);
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            } catch (Exception e) {
                log.error("Error while sending pipeline websocket message", e);
                // send minimal fallback so frontend can render empty stages
                try {
                    if (session.isOpen()) {
                        List<Map<String, Object>> fallback = new ArrayList<>();
                        for (EventStage stage : EventStage.values()) {
                            Map<String, Object> stageData = new HashMap<>();
                            stageData.put("name", stage.name());
                            stageData.put("count", 0);
                            stageData.put("latencyMs", 0);
                            stageData.put("successRate", 0.0);
                            fallback.add(stageData);
                        }
                        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(fallback)));
                    }
                } catch (Exception ex) {
                    log.error("Failed to send pipeline fallback", ex);
                }
            }
        }, 0, 3, TimeUnit.SECONDS);

        session.getAttributes().put("telemetryFuture", future);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
            throws Exception {
        Object f = session.getAttributes().get("telemetryFuture");
        if (f instanceof ScheduledFuture) {
            try {
                ((ScheduledFuture<?>) f).cancel(true);
            } catch (Exception e) {
                log.warn("Failed to cancel pipeline telemetry future for closed session", e);
            }
        }
        super.afterConnectionClosed(session, status);
    }

    private List<Map<String, Object>> generatePipelineData() {
        List<Map<String, Object>> pipeline = new ArrayList<>();

        try {
            Map<EventStage, PipelineStageMetrics> stageMetrics = pipelineService.fullPipeline();

            for (EventStage stage : EventStage.values()) {
                PipelineStageMetrics metrics = stageMetrics.get(stage);
                if (metrics != null) {
                    Map<String, Object> stageData = new HashMap<>();
                    stageData.put("name", stage.name());
                    stageData.put("count", metrics.getCount());
                    stageData.put("latencyMs", metrics.getAvgLatencyMs());
                    stageData.put("successRate", metrics.getSuccessRate());
                    pipeline.add(stageData);
                }
            }
        } catch (Exception e) {
            log.error("Error occured while getting data from Pipeline Service: {}", e);
        }

        return pipeline;
    }
}