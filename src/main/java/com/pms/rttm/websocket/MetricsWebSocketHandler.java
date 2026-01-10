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
public class MetricsWebSocketHandler extends TextWebSocketHandler {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Map<String, Object>> metrics = generateMetrics();
                String json = objectMapper.writeValueAsString(metrics);
                session.sendMessage(new TextMessage(json));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    private List<Map<String, Object>> generateMetrics() {
        List<Map<String, Object>> metrics = new ArrayList<>();
        
        metrics.add(createMetric("Current TPS", (int)(Math.random() * 100), "tx/s", getRandomStatus()));
        metrics.add(createMetric("Peak TPS", (int)(Math.random() * 50), "tx/s", getRandomStatus()));
        metrics.add(createMetric("Avg Latency", (int)(Math.random() * 200), "ms", getRandomStatus()));
        metrics.add(createMetric("DLQ Count", (int)(Math.random() * 20), "errors", getRandomStatus()));
        metrics.add(createMetric("Kafka Lag", (int)(Math.random() * 1000), "msgs", getRandomStatus()));
        
        return metrics;
    }
    
    private Map<String, Object> createMetric(String title, int value, String unit, String status) {
        Map<String, Object> metric = new HashMap<>();
        metric.put("title", title);
        metric.put("value", value);
        metric.put("unit", unit);
        metric.put("status", status);
        return metric;
    }
    
    private String getRandomStatus() {
        String[] statuses = {"healthy", "warning", "critical"};
        return statuses[(int)(Math.random() * statuses.length)];
    }
}