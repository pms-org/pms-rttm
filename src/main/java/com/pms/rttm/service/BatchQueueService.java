package com.pms.rttm.service;

import com.pms.rttm.entity.RttmDlqEventEntity;
import com.pms.rttm.entity.RttmErrorEventEntity;
import com.pms.rttm.entity.RttmQueueMetricEntity;
import com.pms.rttm.entity.RttmTradeEventEntity;
import com.pms.rttm.mapper.DlqEventMapper;
import com.pms.rttm.mapper.ErrorEventMapper;
import com.pms.rttm.mapper.QueueMetricMapper;
import com.pms.rttm.mapper.TradeEventMapper;
import com.pms.rttm.proto.RttmDlqEvent;
import com.pms.rttm.proto.RttmErrorEvent;
import com.pms.rttm.proto.RttmQueueMetric;
import com.pms.rttm.proto.RttmTradeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchQueueService {

    private final RttmIngestService ingestService;

    @Value("${rttm.queue.capacity:10000}")
    private int queueCapacity;

    @Value("${rttm.batch.size:100}")
    private int batchSize;

    private final LinkedBlockingQueue<RttmTradeEvent> tradeQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<RttmErrorEvent> errorQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<RttmQueueMetric> metricQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<RttmDlqEvent> dlqQueue = new LinkedBlockingQueue<>();

    // Enqueue helpers used by consumers. Offer with timeout to avoid blocking
    // consumer threads.
    public boolean enqueueTrade(RttmTradeEvent event) throws InterruptedException {
        return tradeQueue.offer(event, 500, TimeUnit.MILLISECONDS);
    }

    public boolean enqueueError(RttmErrorEvent event) throws InterruptedException {
        return errorQueue.offer(event, 500, TimeUnit.MILLISECONDS);
    }

    public boolean enqueueMetric(RttmQueueMetric event) throws InterruptedException {
        return metricQueue.offer(event, 500, TimeUnit.MILLISECONDS);
    }

    public boolean enqueueDlq(RttmDlqEvent event) throws InterruptedException {
        return dlqQueue.offer(event, 500, TimeUnit.MILLISECONDS);
    }

    // Scheduled processors. Each drains up to batchSize and calls appropriate batch
    // ingest.
    @Scheduled(fixedDelayString = "${rttm.batch.poll-ms:2000}")
    public void processTradeBatch() {
        try {
            List<RttmTradeEvent> items = new ArrayList<>();
            tradeQueue.drainTo(items, batchSize);
            if (items.isEmpty())
                return;

            List<RttmTradeEventEntity> entities = new ArrayList<>(items.size());
            for (RttmTradeEvent e : items)
                entities.add(TradeEventMapper.toEntity(e));

            try {
                ingestService.ingestBatch(entities);
                log.info("Saved trade batch of {}", entities.size());
            } catch (Exception ex) {
                log.error("DB save failed for trade batch, re-queueing {} items", entities.size(), ex);
                // re-queue attempts — put back in queue (best-effort)
                for (RttmTradeEvent r : items) {
                    try {
                        tradeQueue.put(r);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Unexpected error in trade batch processor", ex);
        }
    }

    @Scheduled(fixedDelayString = "${rttm.batch.poll-ms:2000}")
    public void processErrorBatch() {
        try {
            List<RttmErrorEvent> items = new ArrayList<>();
            errorQueue.drainTo(items, batchSize);
            if (items.isEmpty())
                return;

            List<RttmErrorEventEntity> entities = new ArrayList<>(items.size());
            for (RttmErrorEvent e : items)
                entities.add(ErrorEventMapper.toEntity(e));

            try {
                ingestService.ingestBatchErrors(entities);
                log.info("Saved error batch of {}", entities.size());
            } catch (Exception ex) {
                log.error("DB save failed for error batch, re-queueing {} items", entities.size(), ex);
                for (RttmErrorEvent r : items) {
                    try {
                        errorQueue.put(r);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Unexpected error in error batch processor", ex);
        }
    }

    @Scheduled(fixedDelayString = "${rttm.batch.poll-ms:2000}")
    public void processMetricBatch() {
        try {
            List<RttmQueueMetric> items = new ArrayList<>();
            metricQueue.drainTo(items, batchSize);
            if (items.isEmpty())
                return;

            List<RttmQueueMetricEntity> entities = new ArrayList<>(items.size());
            for (RttmQueueMetric e : items)
                entities.add(QueueMetricMapper.toEntity(e));

            try {
                ingestService.ingestBatchQueueMetrics(entities);
                log.info("Saved queue-metric batch of {}", entities.size());
            } catch (Exception ex) {
                log.error("DB save failed for queue-metric batch, re-queueing {} items", entities.size(), ex);
                for (RttmQueueMetric r : items) {
                    try {
                        metricQueue.put(r);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Unexpected error in metric batch processor", ex);
        }
    }

    @Scheduled(fixedDelayString = "${rttm.batch.poll-ms:2000}")
    public void processDlqBatch() {
        try {
            List<RttmDlqEvent> items = new ArrayList<>();
            dlqQueue.drainTo(items, batchSize);
            if (items.isEmpty())
                return;

            List<RttmDlqEventEntity> entities = new ArrayList<>(items.size());
            for (RttmDlqEvent e : items)
                entities.add(DlqEventMapper.toEntity(e));

            try {
                ingestService.ingestBatchDlqEvents(entities);
                log.info("Saved dlq batch of {}", entities.size());
            } catch (Exception ex) {
                log.error("DB save failed for dlq batch, re-queueing {} items", entities.size(), ex);
                for (RttmDlqEvent r : items) {
                    try {
                        dlqQueue.put(r);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Unexpected error in dlq batch processor", ex);
        }
    }

}
