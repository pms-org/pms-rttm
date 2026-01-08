package com.pms.rttm.mapper;

import com.pms.rttm.entity.RttmTradeEventEntity;
import com.pms.rttm.proto.RttmTradeEvent;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class TradeEventMapper {

    public static RttmTradeEventEntity toEntity(RttmTradeEvent proto) {

        return RttmTradeEventEntity.builder()
                .tradeId(proto.getTradeId())
                .serviceName(proto.getServiceName())
                .eventType(proto.getEventType())
                .eventStage(proto.getEventStage())
                .eventStatus(proto.getEventStatus())
                .sourceQueue(proto.getSourceQueue())
                .targetQueue(proto.getTargetQueue())
                .topicName(proto.getTopicName())
                .consumerGroup(proto.getConsumerGroup())
                .partitionId(proto.getPartitionId())
                .offsetValue(proto.getOffsetValue())
                .eventTime(proto.getEventTime())
                .message(proto.getMessage())
                .build();
    }
}
