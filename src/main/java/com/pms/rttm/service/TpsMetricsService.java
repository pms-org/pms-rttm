package com.pms.rttm.service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.pms.rttm.dto.TpsBucket;
import com.pms.rttm.repository.RttmTradeEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TpsMetricsService {

    private final RttmTradeEventRepository tradeRepo;

    public long peakTps(Duration window) {
        return tradeRepo.findPeakTps(window.getSeconds());
    }

    public long currentTps() {
        // TODO: change time interval back to 5 sec

        return tradeRepo.countByEventTimeAfter(
                // Instant.now().minusSeconds(1)
                Instant.parse("2026-01-09T00:00:00Z") // from jan 9 data
        );
    }

    public List<TpsBucket> tpsTrend(Duration window, String bucket) {
        Instant from = Instant.now().minus(window);

        return switch (bucket.toLowerCase()) {
            case "second" -> tradeRepo.tpsPerSecond(from);
            case "minute" -> tradeRepo.tpsPerMinute(from);
            case "hour" -> tradeRepo.tpsPerHour(from);
            default -> throw new IllegalArgumentException(
                    "Unsupported bucket: " + bucket);
        };
    }
}