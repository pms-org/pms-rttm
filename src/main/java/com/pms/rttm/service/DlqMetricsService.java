package com.pms.rttm.service;

import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.pms.rttm.enums.EventStage;
import com.pms.rttm.repository.RttmDlqEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DlqMetricsService {

    private final RttmDlqEventRepository dlqRepo;

    // Last 24 hours window in seconds
    private static final Long WINDOW_24_HOURS = 86400L;

    // Get total DLQ count for last 24 hours
    public long totalDlq() {
        Instant since = Instant.now().minusSeconds(WINDOW_24_HOURS);
        return dlqRepo.countByEventTimeAfter(since);
    }

    // Get DLQ count by stage for last 24 hours
    public Map<EventStage, Long> dlqByStage() {
        Instant since = Instant.now().minusSeconds(WINDOW_24_HOURS);
        return dlqRepo.countGroupedByStageSince(since);
    }
}
