package com.pms.rttm.consumer;

import com.pms.rttm.service.BatchQueueService;
import com.pms.rttm.config.KafkaTopicsProperties;
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

    private final BatchQueueService batchQueueService;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    @KafkaListener(topics = "#{kafkaTopicsProperties.tradeEvents}", containerFactory = "tradeEventListenerFactory")
    public void consume(RttmTradeEvent event, Acknowledgment ack) {
        try {
            // enqueue into in-memory queue; only ack if enqueue succeeded
            boolean offered = batchQueueService.enqueueTrade(event);
            if (offered) {
                ack.acknowledge();
            } else {
                log.warn("Trade queue is full or timed out, not acknowledging Kafka message: {}", event);
            }
        } catch (Exception ex) {
            log.error("Failed to enqueue RTTM Trade event: {}", event, ex);
            // no ack → retry
        }
    }
}
