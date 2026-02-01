-- SQL script to alter VARCHAR column sizes in existing PostgreSQL database
-- Run this against your RTTM database to fix "value too long" errors

-- Alter rttm_trade_events table
ALTER TABLE rttm_trade_events 
    ALTER COLUMN service_name TYPE VARCHAR(128),
    ALTER COLUMN event_type TYPE VARCHAR(128),
    ALTER COLUMN event_stage TYPE VARCHAR(128),
    ALTER COLUMN event_status TYPE VARCHAR(64),
    ALTER COLUMN source_queue TYPE VARCHAR(256),
    ALTER COLUMN target_queue TYPE VARCHAR(256),
    ALTER COLUMN topic_name TYPE VARCHAR(256),
    ALTER COLUMN consumer_group TYPE VARCHAR(128);

-- Alter rttm_queue_metrics table
ALTER TABLE rttm_queue_metrics 
    ALTER COLUMN service_name TYPE VARCHAR(128),
    ALTER COLUMN topic_name TYPE VARCHAR(256),
    ALTER COLUMN consumer_group TYPE VARCHAR(128);

-- Alter rttm_dlq_events table
ALTER TABLE rttm_dlq_events 
    ALTER COLUMN service_name TYPE VARCHAR(128),
    ALTER COLUMN topic_name TYPE VARCHAR(256),
    ALTER COLUMN original_topic TYPE VARCHAR(256),
    ALTER COLUMN event_stage TYPE VARCHAR(128);

-- Alter rttm_error_events table
ALTER TABLE rttm_error_events 
    ALTER COLUMN service_name TYPE VARCHAR(128),
    ALTER COLUMN error_type TYPE VARCHAR(128),
    ALTER COLUMN event_stage TYPE VARCHAR(128);

-- Alter rttm_alerts table
ALTER TABLE rttm_alerts 
    ALTER COLUMN metric_name TYPE VARCHAR(128),
    ALTER COLUMN service_name TYPE VARCHAR(128),
    ALTER COLUMN severity TYPE VARCHAR(64),
    ALTER COLUMN status TYPE VARCHAR(64);

-- Alter rttm_stage_latency table
ALTER TABLE rttm_stage_latency 
    ALTER COLUMN service_name TYPE VARCHAR(128),
    ALTER COLUMN stage_name TYPE VARCHAR(128);

-- Drop parsed logs table if exists (removed from schema)
DROP TABLE IF EXISTS rttm_parsed_logs CASCADE;

-- Drop alert thresholds table if exists (removed from schema)
DROP TABLE IF EXISTS rttm_alert_thresholds CASCADE;
