package com.pms.rttm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricCard {
    private String title;
    private Object value;
    private String unit;
    private String status; // healthy | warning | critical
}
