package com.pms.rttm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "rttm_alerts", indexes = {
        @Index(name = "idx_alerts_triggered_time", columnList = "triggered_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RttmAlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "metric_name", nullable = false, length = 64)
    private String metricName;

    @Column(name = "service_name", length = 64)
    private String serviceName;

    @Column(name = "current_value", nullable = false)
    private Double currentValue;

    @Column(name = "threshold_value", nullable = false)
    private Double thresholdValue;

    @Column(name = "severity", nullable = false, length = 16)
    private String severity;

    @Column(name = "triggered_time", nullable = false)
    private Instant triggeredTime;

    @Column(name = "status", nullable = false, length = 16)
    private String status;
}
