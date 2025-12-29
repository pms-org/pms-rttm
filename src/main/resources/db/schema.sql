-- Tracks every trade movement across services
CREATE TABLE rttm_trade_events (
    id              BIGSERIAL PRIMARY KEY,
    trade_id        VARCHAR(64) NOT NULL,
    service_name    VARCHAR(64) NOT NULL,
    event_type      VARCHAR(64) NOT NULL,
    event_status    VARCHAR(32) NOT NULL,
    source_queue    VARCHAR(128),
    target_queue    VARCHAR(128),
    topic_name      VARCHAR(128),
    consumer_group  VARCHAR(64),
    partition_id    INT,
    offset_value    BIGINT,
    event_time      TIMESTAMP NOT NULL,
    message         TEXT
);

CREATE INDEX idx_trade_events_time
ON rttm_trade_events (event_time);

CREATE INDEX idx_trade_events_trade
ON rttm_trade_events (trade_id);

CREATE INDEX idx_trade_events_service_time
ON rttm_trade_events (service_name, event_time);

-- Stores queue snapshots for lag calculation.
CREATE TABLE rttm_queue_metrics (
    id               BIGSERIAL PRIMARY KEY,
    service_name     VARCHAR(64) NOT NULL,
    topic_name       VARCHAR(128) NOT NULL,
    partition_id     INT NOT NULL,
    produced_offset  BIGINT NOT NULL,
    consumed_offset  BIGINT NOT NULL,
    consumer_group   VARCHAR(64) NOT NULL,
    snapshot_time    TIMESTAMP NOT NULL
);

CREATE INDEX idx_queue_metrics_time
ON rttm_queue_metrics (snapshot_time);

CREATE INDEX idx_queue_metrics_topic_partition
ON rttm_queue_metrics (topic_name, partition_id);

-- Tracks failed messages redirected to DLQ.
CREATE TABLE rttm_dlq_events (
    id              BIGSERIAL PRIMARY KEY,
    trade_id        VARCHAR(64),
    service_name    VARCHAR(64) NOT NULL,
    topic_name      VARCHAR(128) NOT NULL,
    original_topic  VARCHAR(128),
    reason          TEXT NOT NULL,
    event_time      TIMESTAMP NOT NULL
);

CREATE INDEX idx_dlq_time
ON rttm_dlq_events (event_time);

CREATE INDEX idx_dlq_service
ON rttm_dlq_events (service_name);

-- Captures all processing errors
CREATE TABLE rttm_error_events (
    id              BIGSERIAL PRIMARY KEY,
    trade_id        VARCHAR(64),
    service_name    VARCHAR(64) NOT NULL,
    error_type      VARCHAR(64) NOT NULL,
    error_message   TEXT NOT NULL,
    event_time      TIMESTAMP NOT NULL
);

CREATE INDEX idx_error_time
ON rttm_error_events (event_time);

CREATE INDEX idx_error_service
ON rttm_error_events (service_name);

-- Stores interpreted log information (your special feature)
CREATE TABLE rttm_parsed_logs (
    id              BIGSERIAL PRIMARY KEY,
    service_name    VARCHAR(64) NOT NULL,
    log_level       VARCHAR(16) NOT NULL,
    log_category    VARCHAR(64) NOT NULL,
    log_source      VARCHAR(64),
    trade_id        VARCHAR(64),
    message         TEXT NOT NULL,
    event_time      TIMESTAMP NOT NULL
);

CREATE INDEX idx_logs_time
ON rttm_parsed_logs (event_time);

CREATE INDEX idx_logs_trade
ON rttm_parsed_logs (trade_id);

-- Defines alert rules (configurable)
CREATE TABLE rttm_alert_thresholds (
    id              BIGSERIAL PRIMARY KEY,
    metric_name     VARCHAR(64) NOT NULL,
    service_name    VARCHAR(64),
    threshold_value DOUBLE PRECISION NOT NULL,
    comparison      VARCHAR(8) NOT NULL,
    severity        VARCHAR(16) NOT NULL
);

-- Stores triggered alerts
CREATE TABLE rttm_alerts (
    id              BIGSERIAL PRIMARY KEY,
    metric_name     VARCHAR(64) NOT NULL,
    service_name    VARCHAR(64),
    current_value   DOUBLE PRECISION NOT NULL,
    threshold_value DOUBLE PRECISION NOT NULL,
    severity        VARCHAR(16) NOT NULL,
    triggered_time  TIMESTAMP NOT NULL,
    status          VARCHAR(16) NOT NULL
);

