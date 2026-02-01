package com.pms.rttm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

import com.pms.rttm.enums.EventStage;

@Entity
@Table(name = "rttm_error_events", indexes = {
        @Index(name = "idx_error_time", columnList = "event_time"),
        @Index(name = "idx_error_service", columnList = "service_name")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RttmErrorEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "trade_id", nullable = false)
    private UUID tradeId;

    @Column(name = "service_name", nullable = false, length = 64)
    private String serviceName;

    @Column(name = "error_type", nullable = false, length = 64)
    private String errorType;

    @Column(name = "error_message", nullable = false, columnDefinition = "TEXT")
    private String errorMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_stage", nullable = false, length = 32)
    private EventStage eventStage;

    @Column(name = "event_time", nullable = false)
    private Instant eventTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
