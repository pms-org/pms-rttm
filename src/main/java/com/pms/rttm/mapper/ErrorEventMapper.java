package com.pms.rttm.mapper;

import com.pms.rttm.entity.RttmErrorEventEntity;
import com.pms.rttm.proto.RttmErrorEvent;

public final class ErrorEventMapper {

    private ErrorEventMapper() {
    }

    public static RttmErrorEventEntity toEntity(RttmErrorEvent proto) {

        return RttmErrorEventEntity.builder()
                .tradeId(proto.getTradeId())
                .serviceName(proto.getServiceName())
                .errorType(proto.getErrorType().name())
                .errorMessage(proto.getErrorMessage())
                .eventTime(proto.getOccurredAt())
                .build();
    }
}
