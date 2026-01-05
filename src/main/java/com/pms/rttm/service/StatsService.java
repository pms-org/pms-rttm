package com.pms.rttm.service;

import com.pms.rttm.dto.StageStats;
import com.pms.rttm.dto.PartitionLag;
import com.pms.rttm.dto.DlqStats;
import com.pms.rttm.dto.SystemAlert;
import com.pms.rttm.dto.SystemMetrics;
import com.pms.rttm.dto.TpsTrend;
import com.pms.rttm.repository.RttmTradeEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final JdbcTemplate jdbcTemplate;

    public List<StageStats> getStageStats() {
        String sql = """
            SELECT 
                event_type as stage,
                COUNT(*) as count,
                COALESCE(AVG(EXTRACT(EPOCH FROM (event_time - LAG(event_time) OVER (PARTITION BY trade_id ORDER BY event_time))) * 1000), 0) as latency,
                ROUND((COUNT(CASE WHEN event_status = 'SUCCESS' THEN 1 END) * 100.0 / COUNT(*)), 1) as success_rate
            FROM rttm_trade_events 
            WHERE event_time >= NOW() - INTERVAL '1 hour'
            GROUP BY event_type
            ORDER BY 
                CASE event_type 
                    WHEN 'RECEIVED' THEN 1
                    WHEN 'VALIDATED' THEN 2  
                    WHEN 'ENRICHED' THEN 3
                    WHEN 'COMMITTED' THEN 4
                    WHEN 'ANALYZED' THEN 5
                    ELSE 6
                END
            """;
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> 
            new StageStats(
                rs.getString("stage"),
                rs.getLong("count"),
                rs.getDouble("latency"),
                rs.getDouble("success_rate")
            )
        );
    }

    public List<PartitionLag> getPartitionLags() {
        String sql = """
            SELECT 
                CONCAT('p', partition_id) as partition,
                (produced_offset - consumed_offset) as lag
            FROM rttm_queue_metrics 
            WHERE topic_name = 'lifecycle.event'
            AND snapshot_time = (SELECT MAX(snapshot_time) FROM rttm_queue_metrics)
            ORDER BY partition_id
            """;
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> 
            new PartitionLag(
                rs.getString("partition"),
                rs.getLong("lag")
            )
        );
    }

    public DlqStats getDlqStats() {
        // Get total DLQ count
        Long totalCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM rttm_dlq_events WHERE event_time >= NOW() - INTERVAL '24 hours'",
            Long.class
        );
        
        // Get last error time
        String lastError = jdbcTemplate.queryForObject(
            "SELECT CASE WHEN MAX(event_time) IS NULL THEN 'No errors' ELSE CONCAT(EXTRACT(EPOCH FROM (NOW() - MAX(event_time)))/60, ' mins ago') END FROM rttm_dlq_events",
            String.class
        );
        
        // Get errors by stage from error_events table (assuming stage info is in error_type)
        Map<String, Long> errorsByStage = jdbcTemplate.query(
            "SELECT error_type as stage, COUNT(*) as count FROM rttm_error_events WHERE event_time >= NOW() - INTERVAL '24 hours' GROUP BY error_type",
            rs -> {
                Map<String, Long> map = new java.util.HashMap<>();
                while (rs.next()) {
                    map.put(rs.getString("stage"), rs.getLong("count"));
                }
                return map;
            }
        );
        
        return new DlqStats(totalCount, lastError, errorsByStage);
    }

    public List<SystemAlert> getSystemAlerts() {
        String sql = """
            SELECT 
                severity,
                CONCAT(EXTRACT(EPOCH FROM (NOW() - triggered_time))/60, 'm ago') as time_ago,
                CASE 
                    WHEN metric_name = 'kafka_lag' THEN CONCAT('Kafka lag exceeding threshold on P', service_name)
                    WHEN metric_name = 'dlq_count' THEN 'DLQ count increased by 15%'
                    WHEN metric_name = 'latency_spike' THEN CONCAT('Latency spike detected in ', service_name, ' stage')
                    WHEN metric_name = 'partition_rebalance' THEN 'Partition rebalance completed'
                    ELSE metric_name
                END as message
            FROM rttm_alerts 
            WHERE status = 'ACTIVE' 
            AND triggered_time >= NOW() - INTERVAL '1 hour'
            ORDER BY triggered_time DESC
            LIMIT 10
            """;
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> 
            new SystemAlert(
                rs.getString("severity"),
                rs.getString("time_ago"),
                rs.getString("message")
            )
        );
    }

    public SystemMetrics getSystemMetrics() {
        // Current TPS - events in last minute
        Long currentTps = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM rttm_trade_events WHERE event_time >= NOW() - INTERVAL '1 minute'",
            Long.class
        );
        
        // Peak TPS - max events per minute in last hour
        Long peakTps = jdbcTemplate.queryForObject(
            "SELECT MAX(cnt) FROM (SELECT COUNT(*) as cnt FROM rttm_trade_events WHERE event_time >= NOW() - INTERVAL '1 hour' GROUP BY DATE_TRUNC('minute', event_time)) t",
            Long.class
        );
        
        // Average latency from message details
        Double avgLatency = jdbcTemplate.queryForObject(
            "SELECT COALESCE(AVG(CAST(REGEXP_REPLACE(message, '[^0-9.]', '', 'g') AS NUMERIC)), 45.0) FROM rttm_trade_events WHERE message LIKE '%ms%' AND event_time >= NOW() - INTERVAL '10 minutes'",
            Double.class
        );
        
        // DLQ count
        Long dlqCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM rttm_dlq_events WHERE event_time >= NOW() - INTERVAL '1 hour'",
            Long.class
        );
        
        // Total Kafka lag across all partitions
        Long kafkaLag = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(produced_offset - consumed_offset), 0) FROM rttm_queue_metrics WHERE topic_name = 'lifecycle.event' AND snapshot_time = (SELECT MAX(snapshot_time) FROM rttm_queue_metrics)",
            Long.class
        );
        
        return new SystemMetrics(currentTps, peakTps, avgLatency, dlqCount, kafkaLag);
    }

    public List<TpsTrend> getTpsTrend() {
        String sql = """
            SELECT 
                CONCAT('t', ROW_NUMBER() OVER (ORDER BY minute_time)) as time_point,
                COALESCE(COUNT(*), 0) as tps_value
            FROM (
                SELECT DATE_TRUNC('minute', event_time) as minute_time
                FROM rttm_trade_events 
                WHERE event_time >= NOW() - INTERVAL '10 minutes'
                GROUP BY DATE_TRUNC('minute', event_time)
                ORDER BY minute_time
            ) t
            RIGHT JOIN (
                SELECT generate_series(
                    DATE_TRUNC('minute', NOW() - INTERVAL '10 minutes'),
                    DATE_TRUNC('minute', NOW()),
                    '1 minute'::interval
                ) as minute_time
            ) series ON t.minute_time = series.minute_time
            GROUP BY series.minute_time
            ORDER BY series.minute_time
            """;
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> 
            new TpsTrend(
                rs.getString("time_point"),
                rs.getLong("tps_value")
            )
        );
    }
}