package com.pms.rttm.consumer;

import com.pms.rttm.service.RttmIngestService;
import com.pms.rttm.entity.RttmDlqEventEntity;
import com.pms.rttm.entity.RttmTradeEventEntity;
import com.pms.rttm.mapper.DlqEventMapper;
import com.pms.rttm.mapper.TradeEventMapper;
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

    private final RttmIngestService ingestService;

    @KafkaListener(topics = "rttm.dlq.events", containerFactory = "dlqListenerFactory")
    public void consume(RttmDlqEvent event, Acknowledgment ack) {

        try {
            RttmDlqEventEntity eventEntity = DlqEventMapper.toEntity(event);
            ingestService.ingest(eventEntity);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to ingest RTTM DLQ event: {}", event, ex);
            // no ack → Kafka retry
        }
    }
}
