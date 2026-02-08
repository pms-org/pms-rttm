package com.pms.rttm.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.pms.rttm.repository.RttmInvalidTradeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvalidTradeMetricsService {

    private final RttmInvalidTradeRepository invalidTradeRepo;

    // Last 24 hours window in seconds
    private static final Long WINDOW_24_HOURS = 86400L;

    // Get invalid trades count for last 24 hours
    public long invalidTradesCount() {
        Instant since = Instant.now().minusSeconds(WINDOW_24_HOURS);
        return invalidTradeRepo.countSince(since);
    }

    // Get invalid trades count (all-time)
    public long invalidTradesCountAllTime() {
        return invalidTradeRepo.count();
    }
}
