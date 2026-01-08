package com.pms.rttm.consumer;

import com.pms.rttm.service.RttmIngestService;
import com.pms.rttm.entity.RttmErrorEventEntity;
import com.pms.rttm.entity.RttmTradeEventEntity;
import com.pms.rttm.mapper.ErrorEventMapper;
import com.pms.rttm.mapper.TradeEventMapper;
import com.pms.rttm.proto.RttmTradeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeEventConsumer {

    private final RttmIngestService service;

    @KafkaListener(topics = "rttm.trade.events", containerFactory = "tradeEventListenerFactory")
    public void consume(RttmTradeEvent event, Acknowledgment ack) {
        try {
            RttmTradeEventEntity eventEntity = TradeEventMapper.toEntity(event);
            service.ingest(eventEntity);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to ingest RTTM Trade event: {}", event, ex);
            // no ack → retry
        }
    }
}
