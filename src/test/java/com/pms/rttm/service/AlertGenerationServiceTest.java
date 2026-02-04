package com.pms.rttm.service;

import com.pms.rttm.entity.RttmAlertEntity;
import com.pms.rttm.enums.EventStage;
import com.pms.rttm.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertGenerationServiceTest {

    @Mock
    private RttmAlertRepository alertRepository;

    @Mock
    private RttmStageLatencyRepository stageLatencyRepository;

    @Mock
    private RttmErrorEventRepository errorEventRepository;

    @Mock
    private RttmQueueMetricRepository queueMetricRepository;

    @Mock
    private RttmDlqEventRepository dlqEventRepository;

    @Mock
    private RttmTradeEventRepository tradeEventRepository;

    @InjectMocks
    private AlertGenerationService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "latencyWarningThresholdMs", 1000L);
        ReflectionTestUtils.setField(service, "latencyCriticalThresholdMs", 3000L);
        ReflectionTestUtils.setField(service, "errorRateWarningThreshold", 5L);
        ReflectionTestUtils.setField(service, "errorRateCriticalThreshold", 20L);
        ReflectionTestUtils.setField(service, "dlqWarningThreshold", 10L);
        ReflectionTestUtils.setField(service, "dlqCriticalThreshold", 50L);
        ReflectionTestUtils.setField(service, "queueDepthWarningThreshold", 1000L);
        ReflectionTestUtils.setField(service, "queueDepthCriticalThreshold", 5000L);
        ReflectionTestUtils.setField(service, "tpsWarningThreshold", 10000L);
        ReflectionTestUtils.setField(service, "tpsCriticalThreshold", 15000L);
    }

    @Test
    void evaluateAndGenerateAlerts_withCriticalLatency_createsAlert() {
        when(stageLatencyRepository.avgLatency(eq(EventStage.ENRICHED), any(), any())).thenReturn(3500L);
        when(stageLatencyRepository.avgLatency(eq(EventStage.RECEIVED), any(), any())).thenReturn(0L);
        when(stageLatencyRepository.avgLatency(eq(EventStage.VALIDATED), any(), any())).thenReturn(0L);
        when(stageLatencyRepository.avgLatency(eq(EventStage.COMMITTED), any(), any())).thenReturn(0L);
        when(errorEventRepository.countByEventTimeAfter(any())).thenReturn(0L);
        when(dlqEventRepository.countByEventTimeAfter(any())).thenReturn(0L);
        when(queueMetricRepository.findMaxQueueDepthSince(any())).thenReturn(0L);
        when(tradeEventRepository.findPeakTps(anyLong())).thenReturn(0L);

        List<RttmAlertEntity> alerts = service.evaluateAndGenerateAlerts();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getMetricName()).isEqualTo("LATENCY_P99");
        assertThat(alerts.get(0).getServiceName()).isEqualTo("ENRICHED");
        assertThat(alerts.get(0).getSeverity()).isEqualTo("CRITICAL");
        assertThat(alerts.get(0).getCurrentValue()).isEqualTo(3500.0);

        verify(alertRepository).saveAll(any());
    }

    @Test
    void evaluateAndGenerateAlerts_withHighErrorRate_createsAlert() {
        when(stageLatencyRepository.avgLatency(any(), any(), any())).thenReturn(0L);
        when(errorEventRepository.countByEventTimeAfter(any())).thenReturn(25L);
        when(dlqEventRepository.countByEventTimeAfter(any())).thenReturn(0L);
        when(queueMetricRepository.findMaxQueueDepthSince(any())).thenReturn(0L);
        when(tradeEventRepository.findPeakTps(anyLong())).thenReturn(0L);

        List<RttmAlertEntity> alerts = service.evaluateAndGenerateAlerts();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getMetricName()).isEqualTo("ERROR_RATE");
        assertThat(alerts.get(0).getSeverity()).isEqualTo("CRITICAL");
        assertThat(alerts.get(0).getCurrentValue()).isEqualTo(25.0);
    }

    @Test
    void evaluateAndGenerateAlerts_withHighDlqCount_createsAlert() {
        when(stageLatencyRepository.avgLatency(any(), any(), any())).thenReturn(0L);
        when(errorEventRepository.countByEventTimeAfter(any())).thenReturn(0L);
        when(dlqEventRepository.countByEventTimeAfter(any())).thenReturn(60L);
        when(queueMetricRepository.findMaxQueueDepthSince(any())).thenReturn(0L);
        when(tradeEventRepository.findPeakTps(anyLong())).thenReturn(0L);

        List<RttmAlertEntity> alerts = service.evaluateAndGenerateAlerts();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getMetricName()).isEqualTo("DLQ_COUNT");
        assertThat(alerts.get(0).getSeverity()).isEqualTo("CRITICAL");
        assertThat(alerts.get(0).getCurrentValue()).isEqualTo(60.0);
    }

    @Test
    void evaluateAndGenerateAlerts_withHighQueueDepth_createsAlert() {
        when(stageLatencyRepository.avgLatency(any(), any(), any())).thenReturn(0L);
        when(errorEventRepository.countByEventTimeAfter(any())).thenReturn(0L);
        when(dlqEventRepository.countByEventTimeAfter(any())).thenReturn(0L);
        when(queueMetricRepository.findMaxQueueDepthSince(any())).thenReturn(6000L);
        when(tradeEventRepository.findPeakTps(anyLong())).thenReturn(0L);

        List<RttmAlertEntity> alerts = service.evaluateAndGenerateAlerts();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getMetricName()).isEqualTo("QUEUE_LAG");
        assertThat(alerts.get(0).getSeverity()).isEqualTo("CRITICAL");
        assertThat(alerts.get(0).getCurrentValue()).isEqualTo(6000.0);
    }

    @Test
    void evaluateAndGenerateAlerts_withHighTps_createsAlert() {
        when(stageLatencyRepository.avgLatency(any(), any(), any())).thenReturn(0L);
        when(errorEventRepository.countByEventTimeAfter(any())).thenReturn(0L);
        when(dlqEventRepository.countByEventTimeAfter(any())).thenReturn(0L);
        when(queueMetricRepository.findMaxQueueDepthSince(any())).thenReturn(0L);
        when(tradeEventRepository.findPeakTps(anyLong())).thenReturn(16000L);

        List<RttmAlertEntity> alerts = service.evaluateAndGenerateAlerts();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getMetricName()).isEqualTo("TPS");
        assertThat(alerts.get(0).getSeverity()).isEqualTo("CRITICAL");
        assertThat(alerts.get(0).getCurrentValue()).isEqualTo(16000.0);
    }

    @Test
    void evaluateAndGenerateAlerts_withNoThresholdExceeded_createsNoAlerts() {
        when(stageLatencyRepository.avgLatency(any(), any(), any())).thenReturn(500L);
        when(errorEventRepository.countByEventTimeAfter(any())).thenReturn(2L);
        when(dlqEventRepository.countByEventTimeAfter(any())).thenReturn(3L);
        when(queueMetricRepository.findMaxQueueDepthSince(any())).thenReturn(500L);
        when(tradeEventRepository.findPeakTps(anyLong())).thenReturn(5000L);

        List<RttmAlertEntity> alerts = service.evaluateAndGenerateAlerts();

        assertThat(alerts).isEmpty();
        verify(alertRepository, never()).saveAll(any());
    }

    @Test
    void resolveAlert_updatesStatusToResolved() {
        RttmAlertEntity alert = RttmAlertEntity.builder()
                .id(1L)
                .metricName("ERROR_RATE")
                .currentValue(10.0)
                .thresholdValue(5.0)
                .severity("CRITICAL")
                .triggeredTime(Instant.now())
                .status("ACTIVE")
                .build();

        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert));

        service.resolveAlert(1L);

        assertThat(alert.getStatus()).isEqualTo("RESOLVED");
        verify(alertRepository).save(alert);
    }

    @Test
    void autoResolveStaleAlerts_resolvesOldAlerts() {
        Instant old = Instant.now().minus(20, ChronoUnit.MINUTES);
        Instant recent = Instant.now().minus(5, ChronoUnit.MINUTES);

        RttmAlertEntity oldAlert = RttmAlertEntity.builder()
                .id(1L)
                .metricName("ERROR_RATE")
                .triggeredTime(old)
                .status("ACTIVE")
                .build();

        RttmAlertEntity recentAlert = RttmAlertEntity.builder()
                .id(2L)
                .metricName("DLQ_COUNT")
                .triggeredTime(recent)
                .status("ACTIVE")
                .build();

        when(alertRepository.findByStatusOrderByTriggeredTimeDesc(anyString(), any(Pageable.class)))
                .thenReturn(Arrays.asList(oldAlert, recentAlert));

        int resolved = service.autoResolveStaleAlerts(Duration.ofMinutes(15));

        assertThat(resolved).isEqualTo(1);
        assertThat(oldAlert.getStatus()).isEqualTo("AUTO_RESOLVED");
        assertThat(recentAlert.getStatus()).isEqualTo("ACTIVE");
    }
}
