package com.pms.rttm.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QueueMetricConsumer {

    private final QueueMetricIngestService service;

    @KafkaListener(topics = "rttm.queue.metrics", containerFactory = "queueMetricListenerFactory")
    public void consume(RttmQueueMetric metric, Acknowledgment ack) {
        service.ingest(metric);
        ack.acknowledge();
    }
}
