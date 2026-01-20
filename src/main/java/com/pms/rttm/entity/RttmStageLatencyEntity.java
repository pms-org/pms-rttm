package com.pms.rttm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

import com.pms.rttm.enums.EventStage;

@Entity
@Table(name = "rttm_stage_latency", indexes = {
        @Index(name = "idx_latency_time", columnList = "event_time"),
        @Index(name = "idx_latency_stage", columnList = "stage_name")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RttmStageLatencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "trade_id", nullable = false)
    private UUID tradeId;

    @Column(name = "service_name", nullable = false, length = 64)
    private String serviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage_name", nullable = false, length = 32)
    private EventStage stageName;

    @Column(name = "latency_ms", nullable = false)
    private Long latencyMs;

    @Column(name = "event_time", nullable = false)
    private Instant eventTime;
}
