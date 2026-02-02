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
public class TelemetrySnapshotController {

    private final TpsMetricsService tpsMetricsService;
    private final LatencyMetricsService latencyMetricsService;
    private final KafkaLagService kafkaLagService;

    @GetMapping("/telemetry-snapshot")
    public ResponseEntity<RttmAnalysisData> telemetrySnapshot() {

        // TODO: Change back to last 9 mins per minute
        // tps trend (last 10 days, per minute)
        List<Long> tpsTrend = tpsMetricsService.tpsTrend(Duration.ofDays(10), "minute")
                .stream().map(b -> b.getTps()).collect(Collectors.toList());

        // latency metrics for COMMITTED stage (Avg, P95, P99)
        List<LabelValue> latency = new ArrayList<>();
        try {
            var stats = latencyMetricsService.latencyStats(EventStage.COMMITTED);
            latency.add(new LabelValue("Avg", stats.getAvg() == null ? 0L : stats.getAvg().longValue()));
            latency.add(new LabelValue("P95", stats.getP95() == null ? 0L : stats.getP95().longValue()));
            latency.add(new LabelValue("P99", stats.getP99() == null ? 0L : stats.getP99().longValue()));
        } catch (Exception e) {
            log.error("Error while getting Latency metrics: {}", e);
            latency.add(new LabelValue("Avg", 0L));
            latency.add(new LabelValue("P95", 0L));
            latency.add(new LabelValue("P99", 0L));
        }

        // kafka lag per partition
        List<PartitionLag> partitionLags = kafkaLagService.lagByPartition().entrySet().stream()
                .map(e -> new PartitionLag("P" + e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        RttmAnalysisData data = new RttmAnalysisData(tpsTrend, latency, partitionLags);
        return ResponseEntity.ok(data);
    }

}
