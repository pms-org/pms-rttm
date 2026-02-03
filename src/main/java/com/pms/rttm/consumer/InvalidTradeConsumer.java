package com.pms.rttm.consumer;

import com.pms.rttm.service.BatchQueueService;
import com.pms.rttm.config.KafkaTopicsProperties;
import com.pms.validation.proto.InvalidTradeEventProto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvalidTradeConsumer {

    private final BatchQueueService batchQueueService;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    @KafkaListener(topics = "#{kafkaTopicsProperties.invalidTrades}", containerFactory = "invalidTradeListenerFactory")
    public void consume(InvalidTradeEventProto event, Acknowledgment ack) {

        try {
            boolean offered = batchQueueService.enqueueInvalidTrade(event);
            if (offered)
                ack.acknowledge();
            else
                log.warn("Invalid trade queue full/timed out, not acknowledging: {}", event);
        } catch (Exception ex) {
            log.error("Failed to enqueue RTTM invalid trade event: {}", event, ex);
        }
    }
}
