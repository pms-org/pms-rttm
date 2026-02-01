-- Quick Verification Queries for RTTM Services
-- Run these in your PostgreSQL client after services are running

-- 1. Check if trade events are being ingested
SELECT 
    'Trade Events' as table_name,
    COUNT(*) as total_records,
    MAX(event_time) as latest_event
FROM rttm_trade_events;

-- 2. Check stage latencies (should exist after trades)
SELECT 
    'Stage Latencies' as table_name,
    COUNT(*) as total_records,
    MAX(event_time) as latest_event
FROM rttm_stage_latency;

-- 3. Check alerts
SELECT 
    'Alerts' as table_name,
    COUNT(*) as total_records,
    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_alerts
FROM rttm_alerts;

-- 4. Recent trade event stages
SELECT 
    event_stage,
    COUNT(*) as count,
    MAX(event_time) as latest
FROM rttm_trade_events
WHERE event_time >= NOW() - INTERVAL '5 minutes'
GROUP BY event_stage
ORDER BY event_stage;

-- 5. Recent latencies by stage
SELECT 
    stage_name,
    COUNT(*) as count,
    ROUND(AVG(latency_ms)) as avg_ms,
    ROUND(MIN(latency_ms)) as min_ms,
    ROUND(MAX(latency_ms)) as max_ms
FROM rttm_stage_latency
WHERE event_time >= NOW() - INTERVAL '5 minutes'
GROUP BY stage_name
ORDER BY stage_name;

-- 6. Active alerts (if any)
SELECT 
    metric_name,
    service_name,
    current_value,
    threshold_value,
    severity,
    triggered_time
FROM rttm_alerts
WHERE status = 'ACTIVE'
ORDER BY triggered_time DESC;

-- 7. Verify complete trade flow (pick a recent trade)
SELECT 
    t.event_stage,
    t.event_time,
    l.latency_ms,
    l.stage_name as latency_stage
FROM rttm_trade_events t
LEFT JOIN rttm_stage_latency l 
    ON t.trade_id = l.trade_id 
    AND t.event_stage = l.stage_name
WHERE t.trade_id = (
    SELECT trade_id 
    FROM rttm_trade_events 
    ORDER BY event_time DESC 
    LIMIT 1
)
ORDER BY t.event_time;
