package com.pms.rttm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AvgP95P99Latency {

    private long avg;
    private long p95;
    private long p99;
}
