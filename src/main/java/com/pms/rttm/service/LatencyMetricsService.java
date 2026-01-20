package com.pms.rttm.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.pms.rttm.dto.AvgP95P99Latency;
import com.pms.rttm.enums.EventStage;
import com.pms.rttm.repository.RttmStageLatencyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LatencyMetricsService {

    private final RttmStageLatencyRepository latencyRepo;

    public AvgP95P99Latency latencyStats(EventStage stage) {
        return latencyRepo.latencyStats(stage.name());
    }

    public long avgLatency(EventStage stage) {
        return latencyRepo.avgLatency(stage);
    }
}
