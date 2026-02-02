package com.pms.rttm.websocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.rttm.service.*;

import lombok.extern.slf4j.Slf4j;

import com.pms.rttm.enums.EventStage;
import java.time.Duration;
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
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Map<String, Object>> metrics = generateMetrics();
                String json = objectMapper.writeValueAsString(metrics);
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            } catch (Exception e) {
                log.error("Error while sending metrics websocket message", e);
                // send minimal fallback so frontend can render
                try {
                    if (session.isOpen()) {
                        List<Map<String, Object>> fallback = new ArrayList<>();
                        fallback.add(createMetric("Current TPS", 0, "tx/s", "critical"));
                        fallback.add(createMetric("Peak TPS", 0, "tx/s", "critical"));
                        fallback.add(createMetric("Avg Latency", 0, "ms", "critical"));
                        fallback.add(createMetric("DLQ Count", 0, "errors", "healthy"));
                        fallback.add(createMetric("Kafka Lag", 0, "msgs", "healthy"));
                        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(fallback)));
                    }
                } catch (Exception ex) {
                    log.error("Failed to send metrics fallback", ex);
                }
            }
        }, 0, 2, TimeUnit.SECONDS);

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
                log.warn("Failed to cancel metrics telemetry future for closed session", e);
            }
        }
        super.afterConnectionClosed(session, status);
    }

    private List<Map<String, Object>> generateMetrics() {
        List<Map<String, Object>> metrics = new ArrayList<>();

        try {
            long currentTps = tpsService.currentTps();

            // TODO: Change duration to 1 minute
            long peakTps = tpsService.peakTps(Duration.ofDays(10));

            long sum = 0;
            int cnt = 0;
            // Calculate average latency across all stages for last 24 hours
            for (EventStage s : EventStage.values()) {
                try {
                    long v = latencyService.avgLatency(s);
                    sum += v;
                    cnt++;
                } catch (Exception e) {
                    log.error("Error occured while getting Avg Latency: {}", e);
                }
            }
            long avgLatency = (cnt == 0) ? 0 : sum / cnt;
            long dlqCount = dlqService.totalDlq();
            long kafkaLag = lagService.totalLag();

            metrics.add(createMetric("Current TPS", (int) currentTps, "tx/s", getStatus(currentTps, 50)));
            metrics.add(createMetric("Peak TPS", (int) peakTps, "tx/s", getStatus(peakTps, 100)));
            metrics.add(createMetric("Avg Latency", (int) avgLatency, "ms", getLatencyStatus(avgLatency)));
            metrics.add(createMetric("DLQ Count", (int) dlqCount, "errors", getDlqStatus(dlqCount)));
            metrics.add(createMetric("Kafka Lag", (int) kafkaLag, "msgs", getLagStatus(kafkaLag)));
        } catch (Exception e) {
            log.error("Error while getting metrics: {}", e);
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
        if (value > threshold * 2)
            return "critical";
        if (value > threshold)
            return "warning";
        return "healthy";
    }

    private String getLatencyStatus(long latency) {
        if (latency > 500)
            return "critical";
        if (latency > 200)
            return "warning";
        return "healthy";
    }

    private String getDlqStatus(long dlqCount) {
        if (dlqCount > 100)
            return "critical";
        if (dlqCount > 10)
            return "warning";
        return "healthy";
    }

    private String getLagStatus(long lag) {
        if (lag > 10000)
            return "critical";
        if (lag > 1000)
            return "warning";
        return "healthy";
    }
}