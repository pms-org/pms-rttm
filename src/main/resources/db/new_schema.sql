-- RTTM CORE TABLE
-- Tracks every trade movement across services (end-to-end trace)
-- Drives: TPS, pipeline depth, trade tracking, latency derivation
CREATE TABLE rttm_trade_events (
    id              BIGSERIAL PRIMARY KEY,
    trade_id        UUID NOT NULL,               -- Unique trade identifier across entire pipeline
    service_name    VARCHAR(64) NOT NULL,        -- Name of the microservice emitting the event
    event_type      VARCHAR(64) NOT NULL,        -- Technical event (RECEIVED, CONSUMED, PRODUCED, FAILED, ACKED)
    event_stage     VARCHAR(64) NOT NULL,        -- Business stage: RECEIVED / VALIDATED / ENRICHED / COMMITTED / ANALYZED
    event_status    VARCHAR(32) NOT NULL,        -- SUCCESS / FAILED / RETRY / DLQ
    source_queue    VARCHAR(128),                -- Source queue/topic (RabbitMQ or Kafka)
    target_queue    VARCHAR(128),                -- Target queue/topic after processing
    topic_name      VARCHAR(128),                -- Kafka topic name (if applicable)
    consumer_group  VARCHAR(64),                 -- Kafka consumer group name
    partition_id    INT,                         -- Kafka partition number
    offset_value    BIGINT,                      -- Kafka offset processed
    event_time      TIMESTAMP NOT NULL,          -- Time when this event occurred (used for TPS & latency)
    message         TEXT                         -- Optional context or debug message
);

-- Index to support time-based queries (TPS trend, dashboards)
CREATE INDEX idx_trade_events_time
ON rttm_trade_events (event_time);

-- Index to trace a single trade across services
CREATE INDEX idx_trade_events_trade
ON rttm_trade_events (trade_id);

-- Index to query per-service metrics over time
CREATE INDEX idx_trade_events_service_time
ON rttm_trade_events (service_name, event_time);

-- QUEUE METRICS TABLE
-- Stores Kafka offset snapshots
-- Drives: Kafka lag (partition-wise & total)
CREATE TABLE rttm_queue_metrics (
    id               BIGSERIAL PRIMARY KEY,
    service_name     VARCHAR(64) NOT NULL,       -- Consumer service name
    topic_name       VARCHAR(128) NOT NULL,      -- Kafka topic being consumed
    partition_id     INT NOT NULL,               -- Partition number
    produced_offset  BIGINT NOT NULL,            -- Latest offset produced to topic
    consumed_offset  BIGINT NOT NULL,            -- Latest offset consumed by service
    consumer_group   VARCHAR(64) NOT NULL,       -- Consumer group ID
    snapshot_time    TIMESTAMP NOT NULL          -- Time of offset snapshot
);

-- Index to compute lag trends over time
CREATE INDEX idx_queue_metrics_time
ON rttm_queue_metrics (snapshot_time);

-- Index to compute lag per topic-partition
CREATE INDEX idx_queue_metrics_topic_partition
ON rttm_queue_metrics (topic_name, partition_id);

-- DLQ EVENTS TABLE
-- Tracks messages that failed processing and went to DLQ
-- Drives: DLQ count, DLQ trends, error-by-stage
CREATE TABLE rttm_dlq_events (
    id              BIGSERIAL PRIMARY KEY,
    trade_id        UUID NOT NULL,                 -- Trade ID (may be null if failure before trade creation)
    service_name    VARCHAR(64) NOT NULL,        -- Service that sent message to DLQ
    topic_name      VARCHAR(128) NOT NULL,       -- DLQ topic name
    original_topic  VARCHAR(128),                -- Original topic before DLQ redirection
    reason          TEXT NOT NULL,                -- Failure reason
    event_time      TIMESTAMP NOT NULL,          -- When message was sent to DLQ
    event_stage     VARCHAR(32) NOT NULL         -- Pipeline stage where failure occurred
);

-- Index for DLQ time-based dashboards
CREATE INDEX idx_dlq_time
ON rttm_dlq_events (event_time);

-- Index for service-level DLQ analysis
CREATE INDEX idx_dlq_service
ON rttm_dlq_events (service_name);

-- ERROR EVENTS TABLE
-- Captures all runtime and processing errors
-- Drives: Error rate %, alerts, failure analysis
CREATE TABLE rttm_error_events (
    id              BIGSERIAL PRIMARY KEY,
    trade_id        UUID NOT NULL,               -- Trade related to error (if applicable)
    service_name    VARCHAR(64) NOT NULL,        -- Service where error occurred
    error_type      VARCHAR(64) NOT NULL,        -- TECHNICAL / BUSINESS / TIMEOUT / DESERIALIZATION
    error_message   TEXT NOT NULL,               -- Full error description
    event_stage     VARCHAR(64) NOT NULL,
    event_time      TIMESTAMP NOT NULL           -- Time of error occurrence
);

-- Index for error trend analysis
CREATE INDEX idx_error_time
ON rttm_error_events (event_time);

-- Index for service-level error tracking
CREATE INDEX idx_error_service
ON rttm_error_events (service_name);

-- PARSED LOGS TABLE (ADVANCED RTTM FEATURE)
-- Stores interpreted log messages
-- Drives: smart alerts, root cause hints, anomaly detection
CREATE TABLE rttm_parsed_logs (
    id              BIGSERIAL PRIMARY KEY,
    service_name    VARCHAR(64) NOT NULL,        -- Service emitting the log
    log_level       VARCHAR(16) NOT NULL,        -- INFO / WARN / ERROR / DEBUG
    log_category    VARCHAR(64) NOT NULL,        -- KAFKA / DB / RETRY / PERFORMANCE / SECURITY
    log_source      VARCHAR(64),                 -- Class or component name
    trade_id        UUID NOT NULL,                 -- Related trade ID (if available)
    message         TEXT NOT NULL,                -- Parsed and normalized log message
    event_time      TIMESTAMP NOT NULL           -- Log timestamp
);

-- Index for log timeline view
CREATE INDEX idx_logs_time
ON rttm_parsed_logs (event_time);

-- Index for trade-centric log tracing
CREATE INDEX idx_logs_trade
ON rttm_parsed_logs (trade_id);

-- ALERT THRESHOLDS CONFIGURATION 
-- Defines alert rules evaluated by RTTM engine
CREATE TABLE rttm_alert_thresholds (    -- (NOT IN DB)
    id              BIGSERIAL PRIMARY KEY,
    metric_name     VARCHAR(64) NOT NULL,        -- TPS / KAFKA_LAG / DLQ_COUNT / ERROR_RATE / LATENCY_P99
    service_name    VARCHAR(64),                 -- Optional service-specific threshold
    threshold_value DOUBLE PRECISION NOT NULL,   -- Threshold value
    comparison      VARCHAR(8) NOT NULL,         -- >, <, >=, <=
    severity        VARCHAR(16) NOT NULL          -- LOW / MEDIUM / HIGH / CRITICAL
);

-- TRIGGERED ALERTS TABLE
-- Stores actual alert instances shown in RTTM dashboard
CREATE TABLE rttm_alerts (
    id              BIGSERIAL PRIMARY KEY,
    metric_name     VARCHAR(64) NOT NULL,        -- Metric that breached threshold
    service_name    VARCHAR(64),                 -- Affected service
    current_value   DOUBLE PRECISION NOT NULL,   -- Observed value
    threshold_value DOUBLE PRECISION NOT NULL,   -- Configured threshold
    severity        VARCHAR(16) NOT NULL,        -- Alert severity
    triggered_time  TIMESTAMP NOT NULL,          -- When alert was raised
    status          VARCHAR(16) NOT NULL         -- ACTIVE / ACKED / RESOLVED
);

CREATE INDEX IF NOT EXISTS idx_alerts_triggered_time
    ON rttm_alerts (triggered_time);


-- STAGE LATENCY TABLE
-- Stores computed latency per pipeline stage
-- Drives: Avg latency, P95 / P99, stage latency cards
CREATE TABLE rttm_stage_latency (
    id            BIGSERIAL PRIMARY KEY,
    trade_id      UUID NOT NULL,                 -- Trade identifier
    service_name  VARCHAR(64) NOT NULL,          -- Service processing this stage
    stage_name    VARCHAR(32) NOT NULL,          -- RECEIVED / VALIDATED / ENRICHED / COMMITTED / ANALYZED
    latency_ms    BIGINT NOT NULL,               -- Processing latency in milliseconds
    event_time    TIMESTAMP NOT NULL             -- Time when latency was recorded
);

-- Index for latency trend analysis
CREATE INDEX idx_latency_time ON rttm_stage_latency (event_time);

-- Index for stage-level latency dashboards
CREATE INDEX idx_latency_stage ON rttm_stage_latency (stage_name);
