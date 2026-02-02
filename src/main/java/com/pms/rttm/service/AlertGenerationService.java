package com.pms.rttm.service;

import com.pms.rttm.entity.RttmAlertEntity;
import com.pms.rttm.enums.EventStage;
import com.pms.rttm.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertGenerationService {

    private final RttmAlertRepository alertRepository;
    private final RttmStageLatencyRepository stageLatencyRepository;
    private final RttmErrorEventRepository errorEventRepository;
    private final RttmQueueMetricRepository queueMetricRepository;
    private final RttmDlqEventRepository dlqEventRepository;
    private final RttmTradeEventRepository tradeEventRepository;

    @Value("${rttm.alerts.latency.warning.ms:1000}")
    private long latencyWarningThresholdMs;

    @Value("${rttm.alerts.latency.critical.ms:3000}")
    private long latencyCriticalThresholdMs;

    @Value("${rttm.alerts.error-rate.warning:5}")
    private long errorRateWarningThreshold;

    @Value("${rttm.alerts.error-rate.critical:20}")
    private long errorRateCriticalThreshold;

    @Value("${rttm.alerts.dlq.warning:10}")
    private long dlqWarningThreshold;

    @Value("${rttm.alerts.dlq.critical:50}")
    private long dlqCriticalThreshold;

    @Value("${rttm.alerts.queue-depth.warning:1000}")
    private long queueDepthWarningThreshold;

    @Value("${rttm.alerts.queue-depth.critical:5000}")
    private long queueDepthCriticalThreshold;

    @Value("${rttm.alerts.tps.warning:10000}")
    private long tpsWarningThreshold;

    @Value("${rttm.alerts.tps.critical:15000}")
    private long tpsCriticalThreshold;

    @Transactional
    public List<RttmAlertEntity> evaluateAndGenerateAlerts() {
        log.info("Running alert evaluation...");
        List<RttmAlertEntity> newAlerts = new ArrayList<>();
        Instant now = Instant.now();

        newAlerts.addAll(checkStageLatencies(now));
        newAlerts.addAll(checkErrorRates(now));
        newAlerts.addAll(checkDlqCounts(now));
        newAlerts.addAll(checkQueueDepths(now));
        newAlerts.addAll(checkPeakTps(now));

        if (!newAlerts.isEmpty()) {
            alertRepository.saveAll(newAlerts);
            log.info("Generated {} alerts", newAlerts.size());
        } else {
            log.info("No alerts triggered - all metrics within thresholds");
        }

        return newAlerts;
    }

    private List<RttmAlertEntity> checkStageLatencies(Instant triggeredTime) {
        List<RttmAlertEntity> alerts = new ArrayList<>();

        // Check latency for the last 5 minutes (300 seconds)
        Long windowSeconds = 300L;
        Instant since = triggeredTime.minus(5, ChronoUnit.MINUTES);

        for (EventStage stage : EventStage.values()) {
            long avgLatency = stageLatencyRepository.avgLatency(stage, windowSeconds, since);

            if (avgLatency >= latencyCriticalThresholdMs) {
                alerts.add(createAlert("LATENCY_P99", stage.name(), (double) avgLatency,
                        (double) latencyCriticalThresholdMs, "CRITICAL", triggeredTime));
            } else if (avgLatency >= latencyWarningThresholdMs) {
                alerts.add(createAlert("LATENCY_P95", stage.name(), (double) avgLatency,
                        (double) latencyWarningThresholdMs, "HIGH", triggeredTime));
            }
        }
        return alerts;
    }

    private List<RttmAlertEntity> checkErrorRates(Instant triggeredTime) {
        List<RttmAlertEntity> alerts = new ArrayList<>();
        Instant fiveMinutesAgo = triggeredTime.minus(5, ChronoUnit.MINUTES);
        long errorCount = errorEventRepository.countByEventTimeAfter(fiveMinutesAgo);

        if (errorCount >= errorRateCriticalThreshold) {
            alerts.add(createAlert("ERROR_RATE", null, (double) errorCount,
                    (double) errorRateCriticalThreshold, "CRITICAL", triggeredTime));
        } else if (errorCount >= errorRateWarningThreshold) {
            alerts.add(createAlert("ERROR_RATE", null, (double) errorCount,
                    (double) errorRateWarningThreshold, "HIGH", triggeredTime));
        }
        return alerts;
    }

    private List<RttmAlertEntity> checkDlqCounts(Instant triggeredTime) {
        List<RttmAlertEntity> alerts = new ArrayList<>();
        Instant fiveMinutesAgo = triggeredTime.minus(5, ChronoUnit.MINUTES);
        long dlqCount = dlqEventRepository.countByEventTimeAfter(fiveMinutesAgo);

        if (dlqCount >= dlqCriticalThreshold) {
            alerts.add(createAlert("DLQ_COUNT", null, (double) dlqCount,
                    (double) dlqCriticalThreshold, "CRITICAL", triggeredTime));
        } else if (dlqCount >= dlqWarningThreshold) {
            alerts.add(createAlert("DLQ_COUNT", null, (double) dlqCount,
                    (double) dlqWarningThreshold, "HIGH", triggeredTime));
        }
        return alerts;
    }

    private List<RttmAlertEntity> checkQueueDepths(Instant triggeredTime) {
        List<RttmAlertEntity> alerts = new ArrayList<>();
        Instant oneMinuteAgo = triggeredTime.minus(1, ChronoUnit.MINUTES);
        Long maxDepth = queueMetricRepository.findMaxQueueDepthSince(oneMinuteAgo);

        if (maxDepth != null) {
            if (maxDepth >= queueDepthCriticalThreshold) {
                alerts.add(createAlert("QUEUE_LAG", null, maxDepth.doubleValue(),
                        (double) queueDepthCriticalThreshold, "CRITICAL", triggeredTime));
            } else if (maxDepth >= queueDepthWarningThreshold) {
                alerts.add(createAlert("QUEUE_LAG", null, maxDepth.doubleValue(),
                        (double) queueDepthWarningThreshold, "HIGH", triggeredTime));
            }
        }
        return alerts;
    }

    private List<RttmAlertEntity> checkPeakTps(Instant triggeredTime) {
        List<RttmAlertEntity> alerts = new ArrayList<>();
        long peakTps = tradeEventRepository.findPeakTps(60);

        if (peakTps >= tpsCriticalThreshold) {
            alerts.add(createAlert("TPS", null, (double) peakTps,
                    (double) tpsCriticalThreshold, "CRITICAL", triggeredTime));
        } else if (peakTps >= tpsWarningThreshold) {
            alerts.add(createAlert("TPS", null, (double) peakTps,
                    (double) tpsWarningThreshold, "MEDIUM", triggeredTime));
        }
        return alerts;
    }

    private RttmAlertEntity createAlert(String metricName, String serviceName,
            Double currentValue, Double thresholdValue, String severity, Instant triggeredTime) {
        return RttmAlertEntity.builder()
                .metricName(metricName)
                .serviceName(serviceName)
                .currentValue(currentValue)
                .thresholdValue(thresholdValue)
                .severity(severity)
                .triggeredTime(triggeredTime)
                .status("ACTIVE")
                .build();
    }

    @Transactional
    public void resolveAlert(Long alertId) {
        alertRepository.findById(alertId).ifPresent(alert -> {
            alert.setStatus("RESOLVED");
            alertRepository.save(alert);
            log.info("Resolved alert {}", alertId);
        });
    }

    @Transactional
    public int autoResolveStaleAlerts(Duration maxAge) {
        Instant cutoff = Instant.now().minus(maxAge);
        List<RttmAlertEntity> activeAlerts = alertRepository.findByStatusOrderByTriggeredTimeDesc(
                "ACTIVE", Pageable.unpaged());

        int resolved = 0;
        for (RttmAlertEntity alert : activeAlerts) {
            if (alert.getTriggeredTime().isBefore(cutoff)) {
                alert.setStatus("AUTO_RESOLVED");
                resolved++;
            }
        }

        if (resolved > 0) {
            log.info("Auto-resolved {} alerts older than {}", resolved, formatDuration(maxAge));
        } else {
            log.debug("No stale alerts to auto-resolve (checked {} active alerts)", activeAlerts.size());
        }
        return resolved;
    }

    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();

        if (days > 0) {
            return String.format("%d day%s", days, days > 1 ? "s" : "");
        } else if (hours > 0) {
            return String.format("%d hour%s", hours, hours > 1 ? "s" : "");
        } else {
            return String.format("%d minute%s", minutes, minutes > 1 ? "s" : "");
        }
    }
}
