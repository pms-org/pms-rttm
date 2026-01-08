package com.pms.rttm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class TpsBucket {

    private Instant bucketTime;
    private long tps;
}
