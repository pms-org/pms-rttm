package com.pms.rttm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RttmAnalysisData {
    // Only include the three pieces needed for the dashboard initial load
    private List<Long> tpsTrend;
    private List<LabelValue> latencyMetrics;
    private List<PartitionLag> kafkaLag;
}
