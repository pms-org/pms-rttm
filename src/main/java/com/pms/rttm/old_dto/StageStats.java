package com.pms.rttm.old_dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StageStats {
    private String stage;
    private Long count;
    private Double latency;
    private Double successRate;
}