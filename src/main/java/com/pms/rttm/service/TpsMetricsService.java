package com.pms.rttm.service;

import java.time.Duration;
import java.time.Instant;
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
        return tradeRepo.countByCreatedAtAfter(
                Instant.now().minusSeconds(1));
    }

    public List<TpsBucket> tpsTrend(Duration window, String bucket) {
        return tradeRepo.tpsBucketed(
                Instant.now().minus(window),
                bucket);
    }
}