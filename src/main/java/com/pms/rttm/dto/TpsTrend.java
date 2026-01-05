package com.pms.rttm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TpsTrend {
    private String timePoint;
    private Long tpsValue;
}