package com.pms.rttm.websocket;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Component
public class PipelineWebSocketHandler extends TextWebSocketHandler {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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
        String[] stages = {"RECEIVED", "VALIDATED", "ENRICHED", "ANALYZED", "COMMITTED"};
        
        for (String stage : stages) {
            Map<String, Object> stageData = new HashMap<>();
            stageData.put("name", stage);
            stageData.put("count", (int)(Math.random() * 100));
            stageData.put("latencyMs", (int)(Math.random() * 300));
            stageData.put("successRate", Math.random() * 100);
            pipeline.add(stageData);
        }
        
        return pipeline;
    }
}