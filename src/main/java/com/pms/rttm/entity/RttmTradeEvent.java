package com.pms.rttm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "rttm_trade_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RttmTradeEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "trade_id", nullable = false, length = 64)
    private String tradeId;
    
    @Column(name = "service_name", nullable = false, length = 64)
    private String serviceName;
    
    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;
    
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
    private LocalDateTime eventTime;
    
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
}