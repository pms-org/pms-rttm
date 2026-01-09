package com.pms.rttm.mapper;

import java.time.Instant;
import java.util.UUID;

import com.pms.rttm.entity.RttmErrorEventEntity;
import com.pms.rttm.enums.EventStage;
import com.pms.rttm.proto.RttmErrorEvent;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class ErrorEventMapper {

    public static RttmErrorEventEntity toEntity(RttmErrorEvent proto) {

        return RttmErrorEventEntity.builder()
                .tradeId(UUID.fromString(proto.getTradeId()))
                .serviceName(proto.getServiceName())
                .errorType(proto.getErrorType().toString())
                .errorMessage(proto.getErrorMessage())
                .eventStage(EventStage.valueOf(proto.getEventStage()))
                .eventTime(Instant.ofEpochMilli(proto.getEventTime()))
                .build();
    }
}
