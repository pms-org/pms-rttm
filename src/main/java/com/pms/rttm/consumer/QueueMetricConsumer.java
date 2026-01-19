package com.pms.rttm.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.pms.rttm.service.BatchQueueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.pms.rttm.entity.RttmErrorEventEntity;
import com.pms.rttm.entity.RttmQueueMetricEntity;
import com.pms.rttm.mapper.ErrorEventMapper;
import com.pms.rttm.mapper.QueueMetricMapper;
import com.pms.rttm.proto.RttmQueueMetric;

@Component
@Slf4j
@RequiredArgsConstructor
public class QueueMetricConsumer {

    private final BatchQueueService batchQueueService;

    @KafkaListener(topics = "rttm.queue.metrics", containerFactory = "queueMetricListenerFactory")
    public void consume(RttmQueueMetric metric, Acknowledgment ack) {
        try {
            boolean offered = batchQueueService.enqueueMetric(metric);
            if (offered)
                ack.acknowledge();
            else
                log.warn("Metric queue full/timed out, not acknowledging: {}", metric);
        } catch (Exception ex) {
            log.error("Failed to enqueue Queue Metric event: {}", metric, ex);
        }
    }
}
