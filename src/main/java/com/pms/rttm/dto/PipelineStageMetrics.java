package com.pms.rttm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PipelineStageMetrics {

    private long count;
    private long avgLatencyMs;
    private double successRate; // percentage
}
