package com.pms.rttm.websocket;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.rttm.service.*;
import com.pms.rttm.enums.EventStage;
import java.time.Duration;
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
    
    @Autowired
    private TpsMetricsService tpsService;
    
    @Autowired
    private KafkaLagService lagService;
    
    @Autowired
    private LatencyMetricsService latencyService;
    
    @Autowired
    private DlqMetricsService dlqService;

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
        
        try {
            long currentTps = tpsService.currentTps();
            long peakTps = tpsService.peakTps(Duration.ofMinutes(5));
            long avgLatency = latencyService.avgLatency(EventStage.RECEIVED);
            long dlqCount = dlqService.totalDlq();
            long kafkaLag = lagService.totalLag();
            
            metrics.add(createMetric("Current TPS", (int)currentTps, "tx/s", getStatus(currentTps, 50)));
            metrics.add(createMetric("Peak TPS", (int)peakTps, "tx/s", getStatus(peakTps, 100)));
            metrics.add(createMetric("Avg Latency", (int)avgLatency, "ms", getLatencyStatus(avgLatency)));
            metrics.add(createMetric("DLQ Count", (int)dlqCount, "errors", getDlqStatus(dlqCount)));
            metrics.add(createMetric("Kafka Lag", (int)kafkaLag, "msgs", getLagStatus(kafkaLag)));
        } catch (Exception e) {
            // Fallback to sample data if services fail
            metrics.add(createMetric("Current TPS", 0, "tx/s", "critical"));
            metrics.add(createMetric("Peak TPS", 0, "tx/s", "critical"));
            metrics.add(createMetric("Avg Latency", 0, "ms", "critical"));
            metrics.add(createMetric("DLQ Count", 0, "errors", "healthy"));
            metrics.add(createMetric("Kafka Lag", 0, "msgs", "healthy"));
        }
        
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
    
    private String getStatus(long value, long threshold) {
        if (value > threshold * 2) return "critical";
        if (value > threshold) return "warning";
        return "healthy";
    }
    
    private String getLatencyStatus(long latency) {
        if (latency > 500) return "critical";
        if (latency > 200) return "warning";
        return "healthy";
    }
    
    private String getDlqStatus(long dlqCount) {
        if (dlqCount > 100) return "critical";
        if (dlqCount > 10) return "warning";
        return "healthy";
    }
    
    private String getLagStatus(long lag) {
        if (lag > 10000) return "critical";
        if (lag > 1000) return "warning";
        return "healthy";
    }
}