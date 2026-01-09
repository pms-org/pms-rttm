-- rttm_sample_seed.sql
-- Sample seed data for RTTM dashboards
-- NOTE: parsed_logs intentionally excluded

BEGIN;

-- TRADE EVENTS
INSERT INTO rttm_trade_events
(trade_id, service_name, event_type, event_stage, event_status,
 source_queue, target_queue, topic_name, consumer_group,
 partition_id, offset_value, event_time, message)
SELECT
  'TRADE-' || lpad(t.trade::text, 3, '0'),
  s.service_name,
  s.event_type,
  s.event_stage,
  s.event_status,
  s.source_queue,
  s.target_queue,
  s.topic_name,
  s.consumer_group,
  s.partition_id,
  (1000 + t.trade * 10 + s.seq),
  now() - make_interval(secs => (600 - (t.trade * 20 + s.seq * 2))),
  s.message
FROM generate_series(1,10) t(trade)
JOIN (
  VALUES
  (1,'ingest-service','RECEIVED','RECEIVED','SUCCESS',NULL,'trade.validate','trade.ingest','ingest-cg',0,'Received'),
  (2,'validation-service','CONSUMED','VALIDATED','SUCCESS','trade.ingest','trade.enrich','trade.validate','validation-cg',1,'Validated'),
  (3,'enrichment-service','CONSUMED','ENRICHED','SUCCESS','trade.validate','trade.commit','trade.enrich','enrich-cg',0,'Enriched'),
  (4,'commit-service','CONSUMED','COMMITTED','SUCCESS','trade.enrich','trade.analyze','trade.commit','commit-cg',2,'Committed'),
  (5,'analytics-service','CONSUMED','ANALYZED','SUCCESS','trade.commit',NULL,'trade.analyze','analytics-cg',1,'Analyzed')
) s(seq,service_name,event_type,event_stage,event_status,source_queue,target_queue,topic_name,consumer_group,partition_id,message)
ON TRUE;

-- QUEUE METRICS
INSERT INTO rttm_queue_metrics
(service_name, topic_name, partition_id, produced_offset, consumed_offset, consumer_group, snapshot_time)
SELECT
  s.service_name,
  s.topic_name,
  p.partition_id,
  5000 + g * 100 + p.partition_id * 50,
  4900 + g * 95 + p.partition_id * 45,
  s.consumer_group,
  now() - make_interval(secs => (600 - g * 30))
FROM generate_series(1,10) g
JOIN (
  VALUES
  ('validation-service','trade.validate','validation-cg'),
  ('enrichment-service','trade.enrich','enrich-cg'),
  ('commit-service','trade.commit','commit-cg'),
  ('analytics-service','trade.analyze','analytics-cg')
) s(service_name,topic_name,consumer_group)
JOIN (
  VALUES (0),(1),(2)
) p(partition_id) ON TRUE;

-- DLQ EVENTS
INSERT INTO rttm_dlq_events
(trade_id, service_name, topic_name, original_topic, reason, event_time, event_stage)
SELECT
  'TRADE-' || lpad((g % 10 + 1)::text,3,'0'),
  s.service_name,
  'trade.dlq',
  s.original_topic,
  s.reason,
  now() - make_interval(secs => (400 - g * 10)),
  s.event_stage
FROM generate_series(1,50) g
JOIN (
  VALUES
  ('validation-service','trade.validate','Schema validation failed','VALIDATED'),
  ('enrichment-service','trade.enrich','Pricing service timeout','ENRICHED'),
  ('commit-service','trade.commit','DB constraint violation','COMMITTED')
) s(service_name,original_topic,reason,event_stage)
ON TRUE;

-- ERROR EVENTS
INSERT INTO rttm_error_events
(trade_id, service_name, error_type, error_message, event_time)
SELECT
  'TRADE-' || lpad((g % 10 + 1)::text,3,'0'),
  s.service_name,
  s.error_type,
  s.error_message,
  now() - make_interval(secs => (500 - g * 8))
FROM generate_series(1,50) g
JOIN (
  VALUES
  ('validation-service','BUSINESS','Invalid trade amount'),
  ('enrichment-service','TIMEOUT','External service timeout'),
  ('commit-service','TECHNICAL','Duplicate key violation')
) s(service_name,error_type,error_message)
ON TRUE;

-- ALERT THRESHOLDS
INSERT INTO rttm_alert_thresholds
(metric_name, service_name, threshold_value, comparison, severity)
VALUES
('TPS',NULL,200,'<','MEDIUM'),
('KAFKA_LAG','validation-service',10000,'>','HIGH'),
('DLQ_COUNT',NULL,50,'>','CRITICAL'),
('LATENCY_P99','commit-service',3000,'>','HIGH'),
('ERROR_RATE',NULL,5,'>','CRITICAL');

-- ALERTS
INSERT INTO rttm_alerts
(metric_name, service_name, current_value, threshold_value, severity, triggered_time, status)
SELECT
  s.metric_name,
  s.service_name,
  s.current_value,
  s.threshold_value,
  s.severity,
  now() - make_interval(secs => (300 - g * 15)),
  'ACTIVE'
FROM generate_series(1,10) g
JOIN (
  VALUES
  ('DLQ_COUNT','validation-service',60,50,'CRITICAL'),
  ('KAFKA_LAG','commit-service',12000,10000,'HIGH'),
  ('LATENCY_P99','commit-service',3500,3000,'HIGH')
) s(metric_name,service_name,current_value,threshold_value,severity)
ON TRUE;

-- STAGE LATENCY
INSERT INTO rttm_stage_latency
(trade_id, service_name, stage_name, latency_ms, event_time)
SELECT
  'TRADE-' || lpad(t.trade::text,3,'0'),
  s.service_name,
  s.stage_name,
  (100 + random()*4000)::bigint,
  now() - make_interval(secs => (600 - (t.trade * 20 + s.seq * 5)))
FROM generate_series(1,10) t(trade)
JOIN (
  VALUES
  (1,'ingest-service','RECEIVED'),
  (2,'validation-service','VALIDATED'),
  (3,'enrichment-service','ENRICHED'),
  (4,'commit-service','COMMITTED'),
  (5,'analytics-service','ANALYZED')
) s(seq,service_name,stage_name)
ON TRUE;

COMMIT;
