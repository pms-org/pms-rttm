package com.pms.rttm.service;

import com.pms.rttm.entity.*;
import com.pms.rttm.repository.*;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class RttmIngestService {

    private final RttmTradeEventRepository tradeEventRepository;
    private final RttmQueueMetricRepository queueMetricRepository;
    private final RttmDlqEventRepository dlqEventRepository;
    private final RttmErrorEventRepository errorEventRepository;

    public void ingest(RttmTradeEventEntity entity) {
        tradeEventRepository.save(entity);
    }

    public void ingest(RttmQueueMetricEntity entity) {
        queueMetricRepository.save(entity);
    }

    public void ingest(RttmDlqEventEntity entity) {
        dlqEventRepository.save(entity);
    }

    public void ingest(RttmErrorEventEntity entity) {
        errorEventRepository.save(entity);
    }

}
