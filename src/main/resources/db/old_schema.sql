CREATE TABLE trade_state_history (
    id BIGSERIAL PRIMARY KEY,
    trade_id VARCHAR(64) NOT NULL,
    service_name VARCHAR(64) NOT NULL,
    stage VARCHAR(32) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    topic_name VARCHAR(128),
    partition_no INT,
    offset_no BIGINT,
    event_time TIMESTAMP NOT NULL,
    remarks TEXT
);

CREATE INDEX idx_trade_history_trade_time
ON trade_state_history(trade_id, event_time);

CREATE INDEX idx_trade_history_service
ON trade_state_history(service_name);

CREATE TABLE trade_current_state (
    trade_id VARCHAR(64) PRIMARY KEY,
    current_service VARCHAR(64),
    current_stage VARCHAR(32),
    last_event_time TIMESTAMP,
    last_topic VARCHAR(128),
    last_partition INT,
    last_offset BIGINT,
    expected_next_service VARCHAR(64),
    status VARCHAR(32)
);

CREATE TABLE queue_metrics (
    id BIGSERIAL PRIMARY KEY,
    topic_name VARCHAR(128),
    partition_no INT,
    produced_offset BIGINT,
    consumed_offset BIGINT,
    snapshot_time TIMESTAMP
);

CREATE INDEX idx_queue_metrics_topic_time
ON queue_metrics(topic_name, snapshot_time);

CREATE TABLE dlq_events (
    id BIGSERIAL PRIMARY KEY,
    trade_id VARCHAR(64),
    service_name VARCHAR(64),
    topic_name VARCHAR(128),
    reason TEXT,
    event_time TIMESTAMP
);

CREATE TABLE error_events (
    id BIGSERIAL PRIMARY KEY,
    trade_id VARCHAR(64),
    service_name VARCHAR(64),
    error_type VARCHAR(64),
    error_message TEXT,
    event_time TIMESTAMP
);

CREATE TABLE parsed_logs (
    id BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(64),
    trade_id VARCHAR(64),
    log_level VARCHAR(16),
    log_category VARCHAR(32),
    message TEXT,
    event_time TIMESTAMP
);

CREATE TABLE alert_thresholds (
    metric_name VARCHAR(64),
    service_name VARCHAR(64),
    threshold_value DOUBLE PRECISION,
    comparison VARCHAR(8), -- >, <, >=
    PRIMARY KEY (metric_name, service_name)
);

CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    metric_name VARCHAR(64),
    service_name VARCHAR(64),
    current_value DOUBLE PRECISION,
    threshold_value DOUBLE PRECISION,
    status VARCHAR(16), -- ACTIVE, CLEARED
    triggered_time TIMESTAMP
);
