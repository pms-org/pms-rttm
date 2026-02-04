package com.pms.rttm.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pms.rttm.dto.TradeTrackingDto;
import com.pms.rttm.repository.RttmInvalidTradeRepository;
import com.pms.rttm.repository.RttmTradeEventRepository;

@Service
public class TradeTrackingService {

    @Autowired
    RttmTradeEventRepository rttmTradeEventRepository;

    public List<TradeTrackingDto> getTrackTradeByTradeId(UUID tradeId) {
        return rttmTradeEventRepository.findTradeTrackByTradeId(tradeId).stream().map(r -> new TradeTrackingDto(
                ((Number) r[0]).longValue(),
                (UUID) r[1],
                (String) r[2],
                (String) r[3],
                ((Instant) r[4]),
                (String) r[5],
                (String) r[6]))
                .toList();
    }
}
