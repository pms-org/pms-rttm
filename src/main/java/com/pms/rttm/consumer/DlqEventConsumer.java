package com.pms.rttm.consumer;

import com.pms.rttm.service.BatchQueueService;
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

    private final BatchQueueService batchQueueService;

    @KafkaListener(topics = "rttm.dlq.events", containerFactory = "dlqListenerFactory")
    public void consume(RttmDlqEvent event, Acknowledgment ack) {

        try {
            boolean offered = batchQueueService.enqueueDlq(event);
            if (offered)
                ack.acknowledge();
            else
                log.warn("DLQ queue full/timed out, not acknowledging: {}", event);
        } catch (Exception ex) {
            log.error("Failed to enqueue RTTM DLQ event: {}", event, ex);
        }
    }
}
