package com.pms.rttm.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pms.rttm.dto.LabelValue;
import com.pms.rttm.dto.RttmAnalysisData;
import com.pms.rttm.enums.EventStage;
import com.pms.rttm.service.LatencyMetricsService;
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

    @GetMapping("/telemetry-snapshot")
    public ResponseEntity<RttmAnalysisData> telemetrySnapshot() {

        // TPS trend (last 24 hours, per hour)
        List<Long> tpsTrend = tpsMetricsService.tpsTrend(Duration.ofHours(24), "hour")
                .stream().map(b -> b.getTps()).collect(Collectors.toList());

        // latency metrics for COMMITTED stage (Avg, P95, P99) - Last 24 hours
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

        RttmAnalysisData data = new RttmAnalysisData(tpsTrend, latency);
        return ResponseEntity.ok(data);
    }

}
