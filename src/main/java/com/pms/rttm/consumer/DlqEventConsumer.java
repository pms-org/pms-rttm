package com.pms.rttm.consumer;

import com.pms.rttm.service.DlqEventIngestService;
import com.pms.rttm.proto.RttmDlqEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DlqEventConsumer {

    private final DlqEventIngestService ingestService;

    @KafkaListener(topics = "rttm.dlq.events", containerFactory = "dlqListenerFactory")
    public void consume(RttmDlqEvent event, Acknowledgment ack) {

        try {
            ingestService.ingest(event);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to ingest RTTM DLQ event: {}", event, ex);
            // no ack → Kafka retry
        }
    }
}
