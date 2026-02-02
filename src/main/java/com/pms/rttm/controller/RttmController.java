package com.pms.rttm.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pms.rttm.dto.DlqOverview;
import com.pms.rttm.dto.LabelValue;
import com.pms.rttm.dto.PartitionLag;
import com.pms.rttm.dto.RttmAnalysisData;
import com.pms.rttm.dto.MetricCard;
import com.pms.rttm.dto.PipelineStage;
import com.pms.rttm.dto.PipelineStageMetrics;
import com.pms.rttm.enums.EventStage;
import com.pms.rttm.service.DlqMetricsService;
import com.pms.rttm.service.KafkaLagService;
import com.pms.rttm.service.LatencyMetricsService;
import com.pms.rttm.service.PipelineDepthService;
import com.pms.rttm.service.TpsMetricsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/rttm")
@RequiredArgsConstructor
public class RttmController {

    private final TpsMetricsService tpsMetricsService;
    private final LatencyMetricsService latencyMetricsService;
    private final KafkaLagService kafkaLagService;
    private final DlqMetricsService dlqMetricsService;
    private final PipelineDepthService pipelineDepthService;

    @GetMapping("/metrics")
    public ResponseEntity<List<MetricCard>> metrics() {
        List<MetricCard> cards = new ArrayList<>();

        // Current TPS
        long current = tpsMetricsService.currentTps();
        cards.add(new MetricCard("Current TPS", current, "tx/s", healthForTps(current)));

        // Peak TPS (last minute)
        // TODO: Change duration to 1 minute
        long peak = tpsMetricsService.peakTps(Duration.ofDays(10));
        cards.add(new MetricCard("Peak TPS", peak, "tx/s", healthForTps(peak)));

        // Avg latency: compute simple average across stages - Last 24 hours
        long sum = 0;
        int cnt = 0;
        for (EventStage s : EventStage.values()) {
            try {
                long v = latencyMetricsService.avgLatency(s);
                sum += v;
                cnt++;
            } catch (Exception e) {
                log.error("Error occured while getting Avg Latency: {}", e);
            }
        }
        long avgLatency = (cnt == 0) ? 0 : sum / cnt;
        cards.add(new MetricCard("Avg Latency", avgLatency, "ms", healthForLatency(avgLatency)));

        // DLQ count
        long dlq = dlqMetricsService.totalDlq();
        cards.add(new MetricCard("DLQ Count", dlq, "errors", healthForDlq(dlq)));

        // Kafka lag
        long lag = kafkaLagService.totalLag();
        cards.add(new MetricCard("Kafka Lag", lag, "msgs", healthForLag(lag)));

        return ResponseEntity.ok(cards);
    }

    @GetMapping("/pipeline")
    public ResponseEntity<List<PipelineStage>> pipeline() {
        Map<EventStage, PipelineStageMetrics> map = pipelineDepthService.fullPipeline();

        List<PipelineStage> stages = map.entrySet().stream()
                .map(e -> {
                    EventStage stage = e.getKey();
                    PipelineStageMetrics m = e.getValue();
                    return new PipelineStage(stage.name(), m.getCount(), m.getAvgLatencyMs(), m.getSuccessRate());
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(stages);
    }

    @GetMapping("/dlq")
    public ResponseEntity<DlqOverview> dlq() {
        long total = dlqMetricsService.totalDlq();
        Map<String, Long> byStage = dlqMetricsService.dlqByStage().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));

        DlqOverview res = new DlqOverview(total, byStage);
        return ResponseEntity.ok(res);
    }

    // --- simple heuristics for status (could be replaced by config)
    private String healthForTps(long tps) {
        if (tps <= 0)
            return "critical";
        if (tps < 100)
            return "warning";
        return "healthy";
    }

    private String healthForLatency(long ms) {
        if (ms <= 0)
            return "healthy";
        if (ms > 200)
            return "critical";
        if (ms > 100)
            return "warning";
        return "healthy";
    }

    private String healthForDlq(long count) {
        if (count == 0)
            return "healthy";
        if (count > 100)
            return "critical";
        if (count > 10)
            return "warning";
        return "healthy";
    }

    private String healthForLag(long lag) {
        if (lag <= 0)
            return "healthy";
        if (lag > 2000)
            return "critical";
        if (lag > 500)
            return "warning";
        return "healthy";
    }

}
