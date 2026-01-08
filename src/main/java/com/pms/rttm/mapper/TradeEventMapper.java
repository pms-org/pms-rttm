package com.pms.rttm.mapper;

import java.time.Instant;

import com.pms.rttm.entity.RttmTradeEventEntity;
import com.pms.rttm.enums.EventStage;
import com.pms.rttm.enums.EventType;
import com.pms.rttm.proto.RttmTradeEvent;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class TradeEventMapper {

    public static RttmTradeEventEntity toEntity(RttmTradeEvent proto) {

        return RttmTradeEventEntity.builder()
                .tradeId(proto.getTradeId())
                .serviceName(proto.getServiceName())
                .eventType(EventType.valueOf(proto.getEventType()))
                .eventStage(EventStage.valueOf(proto.getEventStage()))
                .eventStatus(proto.getEventStatus())
                .sourceQueue(proto.getSourceQueue())
                .targetQueue(proto.getTargetQueue())
                .topicName(proto.getTopicName())
                .consumerGroup(proto.getConsumerGroup())
                .partitionId(proto.getPartitionId())
                .offsetValue(proto.getOffsetValue())
                .eventTime(Instant.ofEpochMilli(proto.getEventTime()))
                .message(proto.getMessage())
                .build();
    }
}
