-- ALTER commands to add created_at and updated_at columns to all RTTM tables
-- Run these commands on existing database to add timestamp tracking

-- Add timestamps to rttm_trade_events
ALTER TABLE rttm_trade_events
ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add timestamps to rttm_queue_metrics
ALTER TABLE rttm_queue_metrics
ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add timestamps to rttm_dlq_events
ALTER TABLE rttm_dlq_events
ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add timestamps to rttm_error_events
ALTER TABLE rttm_error_events
ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add timestamps to rttm_alerts
ALTER TABLE rttm_alerts
ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add timestamps to rttm_stage_latency
ALTER TABLE rttm_stage_latency
ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Optional: Create a trigger function to automatically update updated_at column
-- This is an alternative to the @PreUpdate annotation in JPA entities
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for each table (optional, as JPA handles this)
CREATE TRIGGER update_rttm_trade_events_updated_at BEFORE UPDATE ON rttm_trade_events
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_rttm_queue_metrics_updated_at BEFORE UPDATE ON rttm_queue_metrics
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_rttm_dlq_events_updated_at BEFORE UPDATE ON rttm_dlq_events
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_rttm_error_events_updated_at BEFORE UPDATE ON rttm_error_events
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_rttm_alerts_updated_at BEFORE UPDATE ON rttm_alerts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_rttm_stage_latency_updated_at BEFORE UPDATE ON rttm_stage_latency
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
