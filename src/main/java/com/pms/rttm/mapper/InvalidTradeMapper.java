package com.pms.rttm.mapper;

import com.google.protobuf.Timestamp;
import com.pms.rttm.entity.RttmInvalidTradeEntity;
import com.pms.validation.proto.InvalidTradeEventProto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
public class InvalidTradeMapper {

    public RttmInvalidTradeEntity toEntity(InvalidTradeEventProto proto) {
        return RttmInvalidTradeEntity.builder()
                .tradeId(UUID.fromString(proto.getTradeId()))
                .portfolioId(UUID.fromString(proto.getPortfolioId()))
                .symbol(proto.getSymbol())
                .side(proto.getSide())
                .pricePerStock(BigDecimal.valueOf(proto.getPricePerStock()))
                .quantity(proto.getQuantity())
                .tradeTimestamp(toInstant(proto.getTimestamp()))
                .validationErrors(proto.getValidationErrors())
                .eventTime(Instant.now())
                .build();
    }

    private Instant toInstant(Timestamp ts) {
        if (ts == null) {
            return Instant.now();
        }
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
    }
}
