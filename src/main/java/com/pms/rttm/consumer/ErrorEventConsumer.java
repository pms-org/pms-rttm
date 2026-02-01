package com.pms.rttm.consumer;

import com.pms.rttm.service.BatchQueueService;
import com.pms.rttm.config.KafkaTopicsProperties;
import com.pms.rttm.entity.RttmErrorEventEntity;
import com.pms.rttm.mapper.ErrorEventMapper;
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

    private final BatchQueueService batchQueueService;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    @KafkaListener(topics = "#{kafkaTopicsProperties.errorEvents}", containerFactory = "errorListenerFactory")
    public void consume(RttmErrorEvent event, Acknowledgment ack) {

        try {
            boolean offered = batchQueueService.enqueueError(event);
            if (offered)
                ack.acknowledge();
            else
                log.warn("Error queue full/timed out, not acknowledging: {}", event);
        } catch (Exception ex) {
            log.error("Failed to enqueue RTTM error event: {}", event, ex);
        }
    }
}
