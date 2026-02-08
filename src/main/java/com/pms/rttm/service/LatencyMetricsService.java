package com.pms.rttm.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.pms.rttm.dto.AvgP95P99Latency;
import com.pms.rttm.enums.EventStage;
import com.pms.rttm.repository.RttmStageLatencyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LatencyMetricsService {

    private final RttmStageLatencyRepository latencyRepo;

    // Last 24 hours window in seconds
    private static final Long WINDOW_24_HOURS = 86400L;

    // Get latency stats (avg, p95, p99) for a specific stage based on last 24 hours
    public AvgP95P99Latency latencyStats(EventStage stage) {
        return latencyRepo.latencyStats(stage.name(), WINDOW_24_HOURS);
    }

    // Get average latency for a specific stage based on last 24 hours
    public long avgLatency(EventStage stage) {
        Instant since = Instant.now().minusSeconds(WINDOW_24_HOURS);
        return latencyRepo.avgLatency(stage, WINDOW_24_HOURS, since);
    }
}
