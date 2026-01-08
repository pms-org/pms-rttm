package com.pms.rttm.mapper;

import com.pms.rttm.entity.RttmDlqEventEntity;
import com.pms.rttm.proto.RttmDlqEvent;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class DlqEventMapper {

    public static RttmDlqEventEntity toEntity(RttmDlqEvent proto) {

        return RttmDlqEventEntity.builder()
                .tradeId(proto.getTradeId())
                .serviceName(proto.getServiceName())
                .topicName(proto.getTopicName())
                .originalTopic(proto.getOriginalTopic())
                .reason(proto.getReason())
                .eventStage(proto.getEventStage().toString())
                .eventTime(proto.getEventTime())
                .build();
    }
}
