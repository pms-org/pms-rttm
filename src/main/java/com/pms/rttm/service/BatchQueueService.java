package com.pms.rttm.service;

import com.pms.rttm.entity.RttmDlqEventEntity;
import com.pms.rttm.entity.RttmErrorEventEntity;
import com.pms.rttm.entity.RttmInvalidTradeEntity;
import com.pms.rttm.entity.RttmQueueMetricEntity;
import com.pms.rttm.entity.RttmTradeEventEntity;
import com.pms.rttm.mapper.DlqEventMapper;
import com.pms.rttm.mapper.ErrorEventMapper;
import com.pms.rttm.mapper.InvalidTradeMapper;
import com.pms.rttm.mapper.QueueMetricMapper;
import com.pms.rttm.mapper.TradeEventMapper;
import com.pms.rttm.proto.RttmDlqEvent;
import com.pms.rttm.proto.RttmErrorEvent;
import com.pms.validation.proto.InvalidTradeEventProto;
import com.pms.rttm.proto.RttmQueueMetric;
import com.pms.rttm.proto.RttmTradeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchQueueService {

    private final RttmIngestService ingestService;
    private final StageLatencyComputationService latencyService;
    private final QueueMetricAggregationService aggregationService;
    private final InvalidTradeMapper invalidTradeMapper;

    private static final int MAX_RETRIES = 3;

    @Value("${rttm.queue.capacity:10000}")
    private int queueCapacity;

    @Value("${rttm.batch.size:100}")
    private int batchSize;

    private final LinkedBlockingQueue<RttmTradeEvent> tradeQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<RttmErrorEvent> errorQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<RttmQueueMetric> metricQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<RttmDlqEvent> dlqQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<InvalidTradeEventProto> invalidTradeQueue = new LinkedBlockingQueue<>();

    private final Map<String, Integer> tradeRetryCount = new ConcurrentHashMap<>();
    private final Map<String, Integer> errorRetryCount = new ConcurrentHashMap<>();
    private final Map<String, Integer> metricRetryCount = new ConcurrentHashMap<>();
    private final Map<String, Integer> dlqRetryCount = new ConcurrentHashMap<>();
    private final Map<String, Integer> invalidTradeRetryCount = new ConcurrentHashMap<>();

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

    public boolean enqueueInvalidTrade(InvalidTradeEventProto event) throws InterruptedException {
        return invalidTradeQueue.offer(event, 500, TimeUnit.MILLISECONDS);
    }

    @Scheduled(fixedDelayString = "${rttm.batch.poll-ms:2000}")
    public void processTradeBatch() {
        try {
            List<RttmTradeEvent> items = new ArrayList<>();
            tradeQueue.drainTo(items, batchSize);
            if (items.isEmpty())
                return;

            List<RttmTradeEventEntity> entities = new ArrayList<>(items.size());
            Set<UUID> tradeIds = new LinkedHashSet<>();

            for (RttmTradeEvent e : items) {
                RttmTradeEventEntity entity = TradeEventMapper.toEntity(e);
                entities.add(entity);
                tradeIds.add(entity.getTradeId());
            }

            try {
                ingestService.ingestBatch(entities);
                log.info("Saved {} trades", entities.size());

                if (!tradeIds.isEmpty()) {
                    latencyService.computeAndSaveLatenciesBatch(tradeIds);
                }
            } catch (Exception ex) {
                log.error("Trade batch save failed, re-queueing {} items", entities.size(), ex);
                for (RttmTradeEvent r : items) {
                    String eventKey = r.getTradeId();
                    int retries = tradeRetryCount.getOrDefault(eventKey, 0);

                    if (retries < MAX_RETRIES) {
                        tradeRetryCount.put(eventKey, retries + 1);
                        try {
                            tradeQueue.put(r);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    } else {
                        log.error("Max retries ({}) exceeded for trade event: {}, discarding", MAX_RETRIES, eventKey);
                        tradeRetryCount.remove(eventKey);
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
                    String eventKey = r.getTradeId();
                    int retries = errorRetryCount.getOrDefault(eventKey, 0);

                    if (retries < MAX_RETRIES) {
                        errorRetryCount.put(eventKey, retries + 1);
                        try {
                            errorQueue.put(r);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    } else {
                        log.error("Max retries ({}) exceeded for error event: {}, discarding", MAX_RETRIES, eventKey);
                        errorRetryCount.remove(eventKey);
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

            List<RttmQueueMetricEntity> entitiesToStore = new ArrayList<>();
            for (RttmQueueMetric e : items) {
                RttmQueueMetricEntity entity = QueueMetricMapper.toEntity(e);

                // Only store if lag changed significantly or time elapsed
                if (aggregationService.shouldStore(entity)) {
                    entitiesToStore.add(entity);
                }
            }

            if (entitiesToStore.isEmpty()) {
                log.debug("All {} queue metrics filtered out by aggregation", items.size());
                return;
            }

            try {
                ingestService.ingestBatchQueueMetrics(entitiesToStore);
                log.info("Saved {} queue metrics (filtered from {})", entitiesToStore.size(), items.size());
            } catch (Exception ex) {
                log.error("DB save failed for queue-metric batch, re-queueing {} items", entitiesToStore.size(), ex);
                for (RttmQueueMetric r : items) {
                    String eventKey = r.getServiceName() + "-" + r.getTopicName();
                    int retries = metricRetryCount.getOrDefault(eventKey, 0);

                    if (retries < MAX_RETRIES) {
                        metricRetryCount.put(eventKey, retries + 1);
                        try {
                            metricQueue.put(r);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    } else {
                        log.error("Max retries ({}) exceeded for metric event: {}, discarding", MAX_RETRIES, eventKey);
                        metricRetryCount.remove(eventKey);
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
                    String eventKey = r.getTradeId();
                    int retries = dlqRetryCount.getOrDefault(eventKey, 0);

                    if (retries < MAX_RETRIES) {
                        dlqRetryCount.put(eventKey, retries + 1);
                        try {
                            dlqQueue.put(r);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    } else {
                        log.error("Max retries ({}) exceeded for dlq event: {}, discarding", MAX_RETRIES, eventKey);
                        dlqRetryCount.remove(eventKey);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Unexpected error in dlq batch processor", ex);
        }
    }

    @Scheduled(fixedDelayString = "${rttm.batch.poll-ms:2000}")
    public void processInvalidTradeBatch() {
        try {
            List<InvalidTradeEventProto> items = new ArrayList<>();
            invalidTradeQueue.drainTo(items, batchSize);
            if (items.isEmpty())
                return;

            List<RttmInvalidTradeEntity> entities = new ArrayList<>(items.size());
            for (InvalidTradeEventProto e : items)
                entities.add(invalidTradeMapper.toEntity(e));

            try {
                ingestService.ingestBatchInvalidTrades(entities);
                log.info("Saved invalid trade batch of {}", entities.size());
            } catch (Exception ex) {
                log.error("DB save failed for invalid trade batch, re-queueing {} items", entities.size(), ex);
                for (InvalidTradeEventProto r : items) {
                    String eventKey = r.getTradeId();
                    int retries = invalidTradeRetryCount.getOrDefault(eventKey, 0);

                    if (retries < MAX_RETRIES) {
                        invalidTradeRetryCount.put(eventKey, retries + 1);
                        try {
                            invalidTradeQueue.put(r);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    } else {
                        log.error("Max retries ({}) exceeded for invalid trade event: {}, discarding", MAX_RETRIES,
                                eventKey);
                        invalidTradeRetryCount.remove(eventKey);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Unexpected error in invalid trade batch processor", ex);
        }
    }

}