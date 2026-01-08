package com.pms.rttm.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.pms.rttm.enums.EventStage;
import com.pms.rttm.repository.RttmDlqEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DlqMetricsService {

    private final RttmDlqEventRepository dlqRepo;

    public long totalDlq() {
        return dlqRepo.count();
    }

    public Map<EventStage, Long> dlqByStage() {
        return dlqRepo.countGroupedByStage();
    }
}
