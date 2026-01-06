package com.pms.rttm.mapper;

import com.pms.rttm.entity.RttmDlqEventEntity;
import com.pms.rttm.proto.RttmDlqEvent;

public final class DlqEventMapper {

    private DlqEventMapper() {
    }

    public static RttmDlqEventEntity toEntity(RttmDlqEvent proto) {

        return RttmDlqEventEntity.builder()
                .tradeId(proto.getTradeId())
                .serviceName(proto.getServiceName())
                .topicName(proto.getDlqTopic())
                .originalTopic(proto.getOriginalTopic())
                .reason(proto.getErrorMessage())
                .eventStage(proto.getEventStage().name())
                .eventTime(proto.getOccurredAt())
                .build();
    }
}
