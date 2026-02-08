package com.pms.rttm.repository;

import com.pms.rttm.dto.TpsBucket;
import com.pms.rttm.dto.TradeTrackingDto;
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

    @Query(value = """
                SELECT COALESCE(MAX(tps), 0)
                FROM (
                    SELECT COUNT(*) AS tps
                    FROM rttm_trade_events
                    WHERE event_time >= NOW() - (:window || ' seconds')::INTERVAL
                      AND event_stage = 'RECEIVED'
                    GROUP BY date_trunc('second', event_time)
                ) t
            """, nativeQuery = true)
    long findPeakTpsForReceived(@Param("window") long windowInSeconds);

    // @Query("""
    // select new com.pms.rttm.dto.TradeTrackingDto(
    // e.id,
    // e.tradeId,
    // e.serviceName,
    // e.eventStage,
    // e.eventTime,
    // e.message,
    // 'TRADE_EVENT'
    // )
    // from RttmTradeEventEntity e
    // where e.tradeId = :tradeId

    // union all

    // select new com.pms.rttm.dto.TradeTrackingDto(
    // i.id,
    // i.tradeId,
    // 'VALIDATION',
    // 'VALIDATED',
    // i.eventTime,
    // i.validationErrors,
    // 'INVALID'
    // )
    // from RttmInvalidTradeEntity i
    // where i.tradeId = :tradeId

    // union all

    // select new com.pms.rttm.dto.TradeTrackingDto(
    // d.id,
    // d.tradeId,
    // d.serviceName,
    // d.eventStage,
    // d.eventTime,
    // d.reason,
    // 'DLQ'
    // )
    // from RttmDlqEventEntity d
    // where d.tradeId = :tradeId

    // union all

    // select new com.pms.rttm.dto.TradeTrackingDto(
    // er.id,
    // er.tradeId,
    // er.serviceName,
    // er.eventStage,
    // er.eventTime,
    // er.errorMessage,
    // 'ERROR'
    // )
    // from RttmErrorEventEntity er
    // where er.tradeId = :tradeId
    // order by eventTime asc
    // """)
    // List<TradeTrackingDto> findTradeTrackByTradeId(UUID tradeId);

    @Query(value = """
                select
                    e.id,
                    e.trade_id,
                    e.service_name,
                    e.event_stage,
                    e.event_time,
                    e.message,
                    'TRADE_EVENT' as source
                from rttm_trade_events e
                where e.trade_id = :tradeId

                union all

                select
                    i.id,
                    i.trade_id,
                    'VALIDATION',
                    'VALIDATED',
                    i.event_time,
                    i.validation_errors,
                    'INVALID'
                from rttm_invalid_trades i
                where i.trade_id = :tradeId

                union all

                select
                    d.id,
                    d.trade_id,
                    d.service_name,
                    d.event_stage,
                    d.event_time,
                    d.reason,
                    'DLQ'
                from rttm_dlq_events d
                where d.trade_id = :tradeId

                union all

                select
                    er.id,
                    er.trade_id,
                    er.service_name,
                    er.event_stage,
                    er.event_time,
                    er.error_message,
                    'ERROR'
                from rttm_error_events er
                where er.trade_id = :tradeId

                order by event_time asc
            """, nativeQuery = true)
    List<Object[]> findTradeTrackByTradeId(@Param("tradeId") UUID tradeId);

}