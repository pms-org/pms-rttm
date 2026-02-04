package com.pms.rttm.dto;

import java.time.Instant;
import java.util.UUID;

import com.pms.rttm.enums.EventStage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeTrackingDto {

    private Long id;

    private UUID tradeId;

    private String serviceName;

    private String eventStage;

    private Instant eventTime;

    private String message;

    private String sourceTable; // TRADE_EVENT | INVALID | DLQ | ERROR

}
