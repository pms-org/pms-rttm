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
    private final RttmStageLatencyRepository stageLatencyRepository;
    private final RttmAlertRepository alertRepository;

    public void ingest(RttmTradeEventEntity entity) {
        tradeEventRepository.save(entity);
    }

    /** Batch save for trade events */
    public void ingestBatch(Iterable<RttmTradeEventEntity> entities) {
        tradeEventRepository.saveAll(entities);
    }

    public void ingest(RttmQueueMetricEntity entity) {
        queueMetricRepository.save(entity);
    }

    /** Batch save for queue metrics */
    public void ingestBatchQueueMetrics(Iterable<RttmQueueMetricEntity> entities) {
        queueMetricRepository.saveAll(entities);
    }

    public void ingest(RttmDlqEventEntity entity) {
        dlqEventRepository.save(entity);
    }

    /** Batch save for DLQ events */
    public void ingestBatchDlqEvents(Iterable<RttmDlqEventEntity> entities) {
        dlqEventRepository.saveAll(entities);
    }

    public void ingest(RttmErrorEventEntity entity) {
        errorEventRepository.save(entity);
    }

    /** Batch save for error events */
    public void ingestBatchErrors(Iterable<RttmErrorEventEntity> entities) {
        errorEventRepository.saveAll(entities);
    }

    public void ingest(RttmStageLatencyEntity entity) {
        stageLatencyRepository.save(entity);
    }

    /** Batch save for stage latencies */
    public void ingestBatchStageLatencies(Iterable<RttmStageLatencyEntity> entities) {
        stageLatencyRepository.saveAll(entities);
    }

    public void ingest(RttmAlertEntity entity) {
        alertRepository.save(entity);
    }

    /** Batch save for alerts */
    public void ingestBatchAlerts(Iterable<RttmAlertEntity> entities) {
        alertRepository.saveAll(entities);
    }

}
