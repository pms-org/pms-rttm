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

@Repository
public interface RttmTradeEventRepository extends JpaRepository<RttmTradeEventEntity, Long> {

    List<RttmTradeEventEntity> findByTradeIdOrderByEventTimeDesc(String tradeId);

    List<RttmTradeEventEntity> findByServiceNameAndEventTimeBetween(
            String serviceName, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(r) FROM RttmTradeEventEntity r WHERE r.eventStatus = 'FAILED' AND r.eventTime >= :since")
    Long countFailedEventsSince(@Param("since") LocalDateTime since);

    long countByCreatedAtAfter(Instant time);

    @Query("""
                SELECT COUNT(e)
                FROM RttmTradeEventEntity e
                WHERE e.eventStage = :stage
            """)
    long countByStage(@Param("stage") EventStage stage);

    // TPS trend
    @Query("""
                SELECT new com.pms.rttm.service.dto.TpsBucket(
                    FUNCTION('date_trunc', :bucket, e.createdAt),
                    COUNT(e)
                )
                FROM RttmTradeEventEntity e
                WHERE e.createdAt >= :from
                GROUP BY FUNCTION('date_trunc', :bucket, e.createdAt)
                ORDER BY FUNCTION('date_trunc', :bucket, e.createdAt)
            """)
    List<TpsBucket> tpsBucketed(
            @Param("from") Instant from,
            @Param("bucket") String bucket);

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