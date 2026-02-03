package com.pms.rttm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kafka.topics")
public class KafkaTopicsProperties {
    private String dlqEvents;
    private String errorEvents;
    private String queueMetrics;
    private String tradeEvents;
    private String invalidTrades;
}