package com.pms.rttm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PartitionLag {
    private String partition;
    private Long lag;
}