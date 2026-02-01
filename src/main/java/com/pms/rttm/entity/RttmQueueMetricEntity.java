package com.pms.rttm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "rttm_queue_metrics", indexes = {
        @Index(name = "idx_queue_metrics_time", columnList = "snapshot_time"),
        @Index(name = "idx_queue_metrics_topic_partition", columnList = "topic_name,partition_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RttmQueueMetricEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "service_name", nullable = false, length = 64)
    private String serviceName;

    @Column(name = "topic_name", nullable = false, length = 128)
    private String topicName;

    @Column(name = "partition_id", nullable = false)
    private Integer partitionId;

    @Column(name = "produced_offset", nullable = false)
    private Long producedOffset;

    @Column(name = "consumed_offset", nullable = false)
    private Long consumedOffset;

    @Column(name = "consumer_group", nullable = false, length = 64)
    private String consumerGroup;

    @Column(name = "snapshot_time", nullable = false)
    private Instant snapshotTime;

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
