package com.pms.rttm.mapper;

import com.pms.rttm.entity.RttmErrorEventEntity;
import com.pms.rttm.proto.RttmErrorEvent;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class ErrorEventMapper {

    public static RttmErrorEventEntity toEntity(RttmErrorEvent proto) {

        return RttmErrorEventEntity.builder()
                .tradeId(proto.getTradeId())
                .serviceName(proto.getServiceName())
                .errorType(proto.getErrorType().toString())
                .errorMessage(proto.getErrorMessage())
                .eventTime(proto.getEventTime())
                .build();
    }
}
