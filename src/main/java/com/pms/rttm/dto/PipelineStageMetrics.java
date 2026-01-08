package com.pms.rttm.dto;

import ch.qos.logback.core.joran.spi.NoAutoStart;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStageMetrics {

    private long count;
    private long avgLatencyMs;
    private double successRate; // percentage
}
