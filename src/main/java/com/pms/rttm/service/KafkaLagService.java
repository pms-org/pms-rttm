package com.pms.rttm.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.pms.rttm.repository.RttmQueueMetricRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaLagService {

    private final RttmQueueMetricRepository queueRepo;

    public long totalLag() {
        return queueRepo.totalLag();
    }

    public Map<Integer, Long> lagByPartition() {
        return queueRepo.lagByPartition();
    }
}
