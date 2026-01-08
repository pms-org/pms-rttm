package com.pms.rttm.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.pms.rttm.service.RttmIngestService;

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

    private final RttmIngestService service;

    @KafkaListener(topics = "rttm.queue.metrics", containerFactory = "queueMetricListenerFactory")
    public void consume(RttmQueueMetric metric, Acknowledgment ack) {
        try {
            RttmQueueMetricEntity eventEntity = QueueMetricMapper.toEntity(metric);
            service.ingest(eventEntity);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to ingest Queue Metric event: {}", metric, ex);
            // no ack → retry
        }
    }
}
