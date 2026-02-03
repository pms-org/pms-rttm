package com.pms.rttm.repository;

import com.pms.rttm.dto.TpsBucket;
import com.pms.rttm.entity.RttmTradeEventEntity;
import com.pms.rttm.enums.EventStage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RttmTradeEventRepository extends JpaRepository<RttmTradeEventEntity, Long> {

    List<RttmTradeEventEntity> findByTradeIdOrderByEventTimeDesc(UUID tradeId);

    List<RttmTradeEventEntity> findByServiceNameAndEventTimeBetween(
            String serviceName, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(r) FROM RttmTradeEventEntity r WHERE r.eventStatus = 'FAILED' AND r.eventTime >= :since")
    Long countFailedEventsSince(@Param("since") LocalDateTime since);

    long countByEventTimeAfter(Instant time);

    @Query("""
                SELECT COUNT(e)
                FROM RttmTradeEventEntity e
                WHERE e.eventStage = :stage
            """)
    long countByStage(@Param("stage") EventStage stage);

    @Query("""
                SELECT COUNT(e)
                FROM RttmTradeEventEntity e
                WHERE e.eventStage = :stage
                  AND e.eventTime >= :since
            """)
    long countByStageSince(@Param("stage") EventStage stage, @Param("since") Instant since);

    // ================= TPS PER SECOND =================

    @Query("""
                SELECT new com.pms.rttm.dto.TpsBucket(
                    FUNCTION('date_trunc', 'second', e.eventTime),
                    COUNT(e)
                )
                FROM RttmTradeEventEntity e
                WHERE e.eventTime >= :from
                GROUP BY FUNCTION('date_trunc', 'second', e.eventTime)
                ORDER BY FUNCTION('date_trunc', 'second', e.eventTime)
            """)
    List<TpsBucket> tpsPerSecond(@Param("from") Instant from);

    // ================= TPS PER MINUTE =================

    @Query("""
                SELECT new com.pms.rttm.dto.TpsBucket(
                    FUNCTION('date_trunc', 'minute', e.eventTime),
                    COUNT(e)
                )
                FROM RttmTradeEventEntity e
                WHERE e.eventTime >= :from
                GROUP BY FUNCTION('date_trunc', 'minute', e.eventTime)
                ORDER BY FUNCTION('date_trunc', 'minute', e.eventTime)
            """)
    List<TpsBucket> tpsPerMinute(@Param("from") Instant from);

    // ================= TPS PER HOUR =================

    @Query("""
                SELECT new com.pms.rttm.dto.TpsBucket(
                    FUNCTION('date_trunc', 'hour', e.eventTime),
                    COUNT(e)
                )
                FROM RttmTradeEventEntity e
                WHERE e.eventTime >= :from
                GROUP BY FUNCTION('date_trunc', 'hour', e.eventTime)
                ORDER BY FUNCTION('date_trunc', 'hour', e.eventTime)
            """)
    List<TpsBucket> tpsPerHour(@Param("from") Instant from);

    @Query(value = """
                SELECT COALESCE(MAX(tps), 0)
                FROM (
                    SELECT COUNT(*) AS tps
                    FROM rttm_trade_events
                    WHERE event_time >= NOW() - (:window || ' seconds')::INTERVAL
                    GROUP BY date_trunc('second', event_time)
                ) t
            """, nativeQuery = true)
    long findPeakTps(@Param("window") long windowInSeconds);
}