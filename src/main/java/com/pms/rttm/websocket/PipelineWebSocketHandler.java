package com.pms.rttm.websocket;

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
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Map<String, Object>> pipeline = generatePipelineData();
                String json = objectMapper.writeValueAsString(pipeline);
                session.sendMessage(new TextMessage(json));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    private List<Map<String, Object>> generatePipelineData() {
        List<Map<String, Object>> pipeline = new ArrayList<>();

        try {
            Map<EventStage, PipelineStageMetrics> stageMetrics = pipelineService.fullPipeline();

            for (EventStage stage : EventStage.values()) {
                PipelineStageMetrics metrics = stageMetrics.get(stage);
                log.debug(metrics.toString());
                if (metrics != null) {
                    Map<String, Object> stageData = new HashMap<>();
                    stageData.put("name", stage.name());
                    stageData.put("count", metrics.getCount());
                    stageData.put("latencyMs", metrics.getAvgLatencyMs());
                    stageData.put("successRate", metrics.getSuccessRate());
                    pipeline.add(stageData);
                }
            }
            log.debug(pipeline.toString());
        } catch (Exception e) {
            log.error("Error occured while getting data from Pipeline Service: {}", e);
        }

        return pipeline;
    }
}