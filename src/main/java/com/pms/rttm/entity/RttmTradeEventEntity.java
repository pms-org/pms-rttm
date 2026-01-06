package com.pms.rttm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "rttm_trade_events", indexes = {
        @Index(name = "idx_trade_events_time", columnList = "event_time"),
        @Index(name = "idx_trade_events_trade", columnList = "trade_id"),
        @Index(name = "idx_trade_events_service_time", columnList = "service_name,event_time")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RttmTradeEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "trade_id", nullable = false, length = 64)
    private String tradeId;

    @Column(name = "service_name", nullable = false, length = 64)
    private String serviceName;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "event_stage", nullable = false, length = 64)
    private String eventStage;

    @Column(name = "event_status", nullable = false, length = 32)
    private String eventStatus;

    @Column(name = "source_queue", length = 128)
    private String sourceQueue;

    @Column(name = "target_queue", length = 128)
    private String targetQueue;

    @Column(name = "topic_name", length = 128)
    private String topicName;

    @Column(name = "consumer_group", length = 64)
    private String consumerGroup;

    @Column(name = "partition_id")
    private Integer partitionId;

    @Column(name = "offset_value")
    private Long offsetValue;

    @Column(name = "event_time", nullable = false)
    private Instant eventTime;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
}
