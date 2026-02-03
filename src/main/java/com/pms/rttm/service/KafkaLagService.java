package com.pms.rttm.service;

import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.pms.rttm.repository.RttmQueueMetricRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaLagService {

    private final RttmQueueMetricRepository queueRepo;

    // Last 24 hours window in seconds
    private static final Long WINDOW_24_HOURS = 86400L;

    /**
     * Get average total lag for last 24 hours.
     */
    public long totalLag() {
        Instant since = Instant.now().minusSeconds(WINDOW_24_HOURS);
        return queueRepo.avgTotalLagSince(since);
    }

    /**
     * Get average lag by partition for last 24 hours.
     */
    public Map<Integer, Long> lagByPartition() {
        Instant since = Instant.now().minusSeconds(WINDOW_24_HOURS);
        return queueRepo.avgLagByPartitionSince(since);
    }
}
