package com.pms.rttm.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.rttm.dto.*;
import com.pms.rttm.enums.EventStage;
import com.pms.rttm.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RttmTelemetryWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final TpsMetricsService tpsService;
    private final LatencyMetricsService latencyService;
    private final KafkaLagService lagService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                // TPS trend
                sendMessage(session, "TPS_TREND", buildTpsTrend());

                // Latency metrics
                sendMessage(session, "LATENCY_METRICS", buildLatencyMetrics());

                // Kafka lag
                sendMessage(session, "KAFKA_LAG", buildKafkaLag());

            } catch (Exception e) {
                log.error("Error while sending telemetry websocket messages", e);
            }
        }, 0, 3, TimeUnit.SECONDS);

        // store the future so we can cancel it when the session closes
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
                log.warn("Failed to cancel telemetry future for closed session", e);
            }
        }
        super.afterConnectionClosed(session, status);
    }

    private void sendMessage(WebSocketSession session, String type, Object data) {
        try {
            if (session == null || !session.isOpen()) {
                // session closed; nothing to do
                Object f = session != null ? session.getAttributes().get("telemetryFuture") : null;
                if (f instanceof ScheduledFuture) {
                    try {
                        ((ScheduledFuture<?>) f).cancel(true);
                    } catch (Exception cancelEx) {
                        log.debug("Error cancelling scheduled task for closed session", cancelEx);
                    }
                }
                return;
            }

            Map<String, Object> envelope = new HashMap<>();
            envelope.put("type", type);
            envelope.put("timestamp", Instant.now().toString());
            envelope.put("data", data);
            String json = objectMapper.writeValueAsString(envelope);
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            log.error("Failed to send websocket message type={}", type, e);
        }
    }

    private List<Long> buildTpsTrend() {

        // TODO: Change this back to 9 mins
        return tpsService.tpsTrend(Duration.ofDays(10), "minute").stream()
                .map(TpsBucket::getTps).collect(Collectors.toList());
    }

    private List<LabelValue> buildLatencyMetrics() {
        try {
            // Get latency stats for last 24 hours
            var stats = latencyService.latencyStats(EventStage.COMMITTED);
            return Arrays.asList(
                    new LabelValue("Avg", stats.getAvg() == null ? 0L : stats.getAvg().longValue()),
                    new LabelValue("P95", stats.getP95() == null ? 0L : stats.getP95().longValue()),
                    new LabelValue("P99", stats.getP99() == null ? 0L : stats.getP99().longValue()));
        } catch (Exception e) {
            return Arrays.asList(new LabelValue("Avg", 0L), new LabelValue("P95", 0L), new LabelValue("P99", 0L));
        }
    }

    private List<PartitionLag> buildKafkaLag() {
        return lagService.lagByPartition().entrySet().stream()
                .map(e -> new PartitionLag("P" + e.getKey(), e.getValue())).collect(Collectors.toList());
    }

}
