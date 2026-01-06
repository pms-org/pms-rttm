package com.pms.rttm.consumer;

import com.pms.rttm.ingestor.service.TradeEventIngestService;
import com.pms.rttm.proto.RttmEventProto;
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

    private final TradeEventIngestService service;

    @KafkaListener(topics = "rttm.trade.events", containerFactory = "tradeEventListenerFactory")
    public void consume(RttmTradeEvent event, Acknowledgment ack) {
        service.ingest(event);
        ack.acknowledge();
    }
}
