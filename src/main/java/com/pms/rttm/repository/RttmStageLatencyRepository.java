package com.pms.rttm.repository;

import com.pms.rttm.dto.AvgP95P99Latency;
import com.pms.rttm.entity.RttmStageLatencyEntity;
import com.pms.rttm.enums.EventStage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RttmStageLatencyRepository
        extends JpaRepository<RttmStageLatencyEntity, Long> {

    @Query("""
                SELECT COALESCE(AVG(l.latencyMs), 0)
                FROM RttmStageLatencyEntity l
                WHERE l.stageName = :stage
            """)
    long avgLatency(@Param("stage") EventStage stage);

    @Query(value = """
                SELECT
                    AVG(latency_ms)    AS avg,
                    PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY latency_ms) AS p95,
                    PERCENTILE_CONT(0.99) WITHIN GROUP (ORDER BY latency_ms) AS p99
                FROM rttm_stage_latency
                WHERE stage_name = :stage
            """, nativeQuery = true)
    AvgP95P99Latency latencyStats(@Param("stage") String stage);
}
