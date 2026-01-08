package com.pms.rttm.old_dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SystemMetrics {
    private Long currentTps;
    private Long peakTps;
    private Double avgLatency;
    private Long dlqCount;
    private Long kafkaLag;
}