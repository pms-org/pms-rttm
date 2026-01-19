package com.pms.rttm.mapper;

import java.time.Instant;
import java.util.UUID;

import com.pms.rttm.entity.RttmDlqEventEntity;
import com.pms.rttm.enums.EventStage;
import com.pms.rttm.proto.RttmDlqEvent;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class DlqEventMapper {

    public static RttmDlqEventEntity toEntity(RttmDlqEvent proto) {

        return RttmDlqEventEntity.builder()
                .tradeId(UUID.fromString(proto.getTradeId()))
                .serviceName(proto.getServiceName())
                .topicName(proto.getTopicName())
                .originalTopic(proto.getOriginalTopic())
                .reason(proto.getReason())
                .eventStage(EventStage.valueOf(proto.getEventStage()))
                .eventTime(Instant.ofEpochMilli(proto.getEventTime()))
                .build();
    }
}
