package com.pms.rttm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Data
public class TpsBucket {

    private Instant bucketTime;
    private long tps;

    public TpsBucket(Object bucketTime, Long tps) {
        this.tps = (tps != null) ? tps : 0L;

        if (bucketTime == null) {
            this.bucketTime = null;
        } else if (bucketTime instanceof OffsetDateTime odt) {
            this.bucketTime = odt.toInstant();
        } else if (bucketTime instanceof LocalDateTime ldt) {
            // date_trunc usually loses the offset, so we assume UTC or system default
            this.bucketTime = ldt.toInstant(ZoneOffset.UTC);
        } else if (bucketTime instanceof java.sql.Timestamp ts) {
            this.bucketTime = ts.toInstant();
        } else {
            throw new IllegalArgumentException("Unknown date type: " + bucketTime.getClass());
        }
    }
}
