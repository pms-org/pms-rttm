package com.pms.rttm.mapper;

import com.pms.rttm.entity.RttmTradeEventEntity;
import com.pms.rttm.proto.RttmTradeEvent;

public final class TradeEventMapper {

    private TradeEventMapper() {
    }

    public static RttmTradeEventEntity toEntity(RttmTradeEvent proto) {

        return RttmTradeEventEntity.builder()
                .tradeId(proto.getTradeId())
                .serviceName(proto.getServiceName())
                .eventType(proto.getEventType().name())
                .eventStage(proto.getEventStage().name())
                .eventStatus(proto.getEventStatus().name())
                .sourceQueue(proto.getSourceQueue())
                .targetQueue(proto.getTargetQueue())
                .topicName(proto.getTopic())
                .consumerGroup(proto.getConsumerGroup())
                .partitionId(proto.getPartition())
                .offsetValue(proto.getOffset())
                .eventTime(proto.getOccurredAt())
                .message(proto.getMessage())
                .build();
    }
}
