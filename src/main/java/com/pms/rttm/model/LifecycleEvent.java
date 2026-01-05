package com.pms.rttm.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LifecycleEvent {
    
    @JsonProperty("traceId")
    private String traceId;
    
    @JsonProperty("portfolioId") 
    private String portfolioId;
    
    @JsonProperty("stage")
    private String stage;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("details")
    private String details;
    
    @JsonProperty("serviceName")
    private String serviceName;
    
    @JsonProperty("queueName")
    private String queueName;
}