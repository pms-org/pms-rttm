package com.pms.rttm.consumer;

import com.pms.rttm.service.ErrorEventIngestService;
import com.pms.rttm.proto.RttmErrorEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorEventConsumer {

    private final ErrorEventIngestService ingestService;

    @KafkaListener(topics = "rttm.error.events", containerFactory = "errorListenerFactory")
    public void consume(RttmErrorEvent event, Acknowledgment ack) {

        try {
            ingestService.ingest(event);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to ingest RTTM error event: {}", event, ex);
            // no ack → retry
        }
    }
}
