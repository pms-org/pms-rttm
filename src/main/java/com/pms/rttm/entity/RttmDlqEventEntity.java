package com.pms.rttm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "rttm_dlq_events", indexes = {
        @Index(name = "idx_dlq_time", columnList = "event_time"),
        @Index(name = "idx_dlq_service", columnList = "service_name")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RttmDlqEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "trade_id", length = 64)
    private String tradeId;

    @Column(name = "service_name", nullable = false, length = 64)
    private String serviceName;

    @Column(name = "topic_name", nullable = false, length = 128)
    private String topicName;

    @Column(name = "original_topic", length = 128)
    private String originalTopic;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "event_time", nullable = false)
    private Instant eventTime;

    @Column(name = "event_stage", nullable = false, length = 32)
    private String eventStage;
}
