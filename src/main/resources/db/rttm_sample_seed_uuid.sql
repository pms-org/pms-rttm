-- ================= RTTM SAMPLE SEED DATA (UUID BASED) =================

-- rttm_trade_events


INSERT INTO rttm_trade_events
(trade_id, service_name, event_type, event_stage, event_status,
 source_queue, target_queue, topic_name, consumer_group, partition_id, offset_value, event_time, message)
VALUES
('a2c04c8f-aa0b-4f6d-bd56-f103f612fcc1', 'trade-ingest', 'TRADE_RECEIVED', 'RECEIVED', 'SUCCESS',
 'trade.in', 'trade.validate', 'trades.raw', 'cg-ingest', 0, 1000, '2026-01-09 04:05:09', 'Trade received'),

('a2c04c8f-aa0b-4f6d-bd56-f103f612fcc1', 'trade-validator', 'TRADE_VALIDATED', 'VALIDATED', 'SUCCESS',
 'trade.validate', 'trade.enrich', 'trades.validated', 'cg-validate', 1, 2000, '2026-01-09 04:06:09', 'Trade validated'),

('a2c04c8f-aa0b-4f6d-bd56-f103f612fcc1', 'trade-enricher', 'TRADE_ENRICHED', 'ENRICHED', 'SUCCESS',
 'trade.enrich', 'trade.commit', 'trades.enriched', 'cg-enrich', 2, 3000, '2026-01-09 04:07:09', 'Trade enriched'),

('a2c04c8f-aa0b-4f6d-bd56-f103f612fcc1', 'trade-committer', 'TRADE_COMMITTED', 'COMMITTED', 'SUCCESS',
 'trade.commit', 'trade.analyze', 'trades.committed', 'cg-commit', 3, 4000, '2026-01-09 04:08:09', 'Trade committed'),

('a2c04c8f-aa0b-4f6d-bd56-f103f612fcc1', 'trade-analyzer', 'TRADE_ANALYZED', 'ANALYZED', 'SUCCESS',
 'trade.analyze', 'none', 'trades.analyzed', 'cg-analyze', 4, 5000, '2026-01-09 04:09:09', 'Trade analyzed');


INSERT INTO rttm_trade_events
(trade_id, service_name, event_type, event_stage, event_status,
 source_queue, target_queue, topic_name, consumer_group, partition_id, offset_value, event_time, message)
VALUES
('2437b8ca-5d13-428d-b6ea-85fa5647d86e', 'trade-ingest', 'TRADE_RECEIVED', 'RECEIVED', 'SUCCESS',
 'trade.in', 'trade.validate', 'trades.raw', 'cg-ingest', 0, 1001, '2026-01-09 04:06:09', 'Trade received'),

('2437b8ca-5d13-428d-b6ea-85fa5647d86e', 'trade-validator', 'TRADE_VALIDATED', 'VALIDATED', 'SUCCESS',
 'trade.validate', 'trade.enrich', 'trades.validated', 'cg-validate', 1, 2001, '2026-01-09 04:07:09', 'Trade validated'),

('2437b8ca-5d13-428d-b6ea-85fa5647d86e', 'trade-enricher', 'TRADE_ENRICHED', 'ENRICHED', 'SUCCESS',
 'trade.enrich', 'trade.commit', 'trades.enriched', 'cg-enrich', 2, 3001, '2026-01-09 04:08:09', 'Trade enriched'),

('2437b8ca-5d13-428d-b6ea-85fa5647d86e', 'trade-committer', 'TRADE_COMMITTED', 'COMMITTED', 'SUCCESS',
 'trade.commit', 'trade.analyze', 'trades.committed', 'cg-commit', 3, 4001, '2026-01-09 04:09:09', 'Trade committed'),

('2437b8ca-5d13-428d-b6ea-85fa5647d86e', 'trade-analyzer', 'TRADE_ANALYZED', 'ANALYZED', 'SUCCESS',
 'trade.analyze', 'none', 'trades.analyzed', 'cg-analyze', 4, 5001, '2026-01-09 04:10:09', 'Trade analyzed');


INSERT INTO rttm_trade_events
(trade_id, service_name, event_type, event_stage, event_status,
 source_queue, target_queue, topic_name, consumer_group, partition_id, offset_value, event_time, message)
VALUES
('a5ffcea6-31c6-4c10-977d-aa4965d82902', 'trade-ingest', 'TRADE_RECEIVED', 'RECEIVED', 'SUCCESS',
 'trade.in', 'trade.validate', 'trades.raw', 'cg-ingest', 0, 1002, '2026-01-09 04:07:09', 'Trade received'),

('a5ffcea6-31c6-4c10-977d-aa4965d82902', 'trade-validator', 'TRADE_VALIDATED', 'VALIDATED', 'SUCCESS',
 'trade.validate', 'trade.enrich', 'trades.validated', 'cg-validate', 1, 2002, '2026-01-09 04:08:09', 'Trade validated'),

('a5ffcea6-31c6-4c10-977d-aa4965d82902', 'trade-enricher', 'TRADE_ENRICHED', 'ENRICHED', 'SUCCESS',
 'trade.enrich', 'trade.commit', 'trades.enriched', 'cg-enrich', 2, 3002, '2026-01-09 04:09:09', 'Trade enriched'),

('a5ffcea6-31c6-4c10-977d-aa4965d82902', 'trade-committer', 'TRADE_COMMITTED', 'COMMITTED', 'SUCCESS',
 'trade.commit', 'trade.analyze', 'trades.committed', 'cg-commit', 3, 4002, '2026-01-09 04:10:09', 'Trade committed'),

('a5ffcea6-31c6-4c10-977d-aa4965d82902', 'trade-analyzer', 'TRADE_ANALYZED', 'ANALYZED', 'SUCCESS',
 'trade.analyze', 'none', 'trades.analyzed', 'cg-analyze', 4, 5002, '2026-01-09 04:11:09', 'Trade analyzed');


INSERT INTO rttm_trade_events
(trade_id, service_name, event_type, event_stage, event_status,
 source_queue, target_queue, topic_name, consumer_group, partition_id, offset_value, event_time, message)
VALUES
('4a4b1542-d438-4d7d-86d3-8119f25566d2', 'trade-ingest', 'TRADE_RECEIVED', 'RECEIVED', 'SUCCESS',
 'trade.in', 'trade.validate', 'trades.raw', 'cg-ingest', 0, 1003, '2026-01-09 04:08:09', 'Trade received'),

('4a4b1542-d438-4d7d-86d3-8119f25566d2', 'trade-validator', 'TRADE_VALIDATED', 'VALIDATED', 'SUCCESS',
 'trade.validate', 'trade.enrich', 'trades.validated', 'cg-validate', 1, 2003, '2026-01-09 04:09:09', 'Trade validated'),

('4a4b1542-d438-4d7d-86d3-8119f25566d2', 'trade-enricher', 'TRADE_ENRICHED', 'ENRICHED', 'SUCCESS',
 'trade.enrich', 'trade.commit', 'trades.enriched', 'cg-enrich', 2, 3003, '2026-01-09 04:10:09', 'Trade enriched'),

('4a4b1542-d438-4d7d-86d3-8119f25566d2', 'trade-committer', 'TRADE_COMMITTED', 'COMMITTED', 'SUCCESS',
 'trade.commit', 'trade.analyze', 'trades.committed', 'cg-commit', 3, 4003, '2026-01-09 04:11:09', 'Trade committed'),

('4a4b1542-d438-4d7d-86d3-8119f25566d2', 'trade-analyzer', 'TRADE_ANALYZED', 'ANALYZED', 'SUCCESS',
 'trade.analyze', 'none', 'trades.analyzed', 'cg-analyze', 4, 5003, '2026-01-09 04:12:09', 'Trade analyzed');


INSERT INTO rttm_trade_events
(trade_id, service_name, event_type, event_stage, event_status,
 source_queue, target_queue, topic_name, consumer_group, partition_id, offset_value, event_time, message)
VALUES
('2b871eed-c638-436b-8d17-8f98bd64b1ec', 'trade-ingest', 'TRADE_RECEIVED', 'RECEIVED', 'SUCCESS',
 'trade.in', 'trade.validate', 'trades.raw', 'cg-ingest', 0, 1004, '2026-01-09 04:09:09', 'Trade received'),

('2b871eed-c638-436b-8d17-8f98bd64b1ec', 'trade-validator', 'TRADE_VALIDATED', 'VALIDATED', 'SUCCESS',
 'trade.validate', 'trade.enrich', 'trades.validated', 'cg-validate', 1, 2004, '2026-01-09 04:10:09', 'Trade validated'),

('2b871eed-c638-436b-8d17-8f98bd64b1ec', 'trade-enricher', 'TRADE_ENRICHED', 'ENRICHED', 'SUCCESS',
 'trade.enrich', 'trade.commit', 'trades.enriched', 'cg-enrich', 2, 3004, '2026-01-09 04:11:09', 'Trade enriched'),

('2b871eed-c638-436b-8d17-8f98bd64b1ec', 'trade-committer', 'TRADE_COMMITTED', 'COMMITTED', 'SUCCESS',
 'trade.commit', 'trade.analyze', 'trades.committed', 'cg-commit', 3, 4004, '2026-01-09 04:12:09', 'Trade committed'),

('2b871eed-c638-436b-8d17-8f98bd64b1ec', 'trade-analyzer', 'TRADE_ANALYZED', 'ANALYZED', 'SUCCESS',
 'trade.analyze', 'none', 'trades.analyzed', 'cg-analyze', 4, 5004, '2026-01-09 04:13:09', 'Trade analyzed');


INSERT INTO rttm_trade_events
(trade_id, service_name, event_type, event_stage, event_status,
 source_queue, target_queue, topic_name, consumer_group, partition_id, offset_value, event_time, message)
VALUES
('2e68899f-7b7c-4284-8195-4b2531491d94', 'trade-ingest', 'TRADE_RECEIVED', 'RECEIVED', 'SUCCESS',
 'trade.in', 'trade.validate', 'trades.raw', 'cg-ingest', 0, 1005, '2026-01-09 04:10:09', 'Trade received'),

('2e68899f-7b7c-4284-8195-4b2531491d94', 'trade-validator', 'TRADE_VALIDATED', 'VALIDATED', 'SUCCESS',
 'trade.validate', 'trade.enrich', 'trades.validated', 'cg-validate', 1, 2005, '2026-01-09 04:11:09', 'Trade validated'),

('2e68899f-7b7c-4284-8195-4b2531491d94', 'trade-enricher', 'TRADE_ENRICHED', 'ENRICHED', 'SUCCESS',
 'trade.enrich', 'trade.commit', 'trades.enriched', 'cg-enrich', 2, 3005, '2026-01-09 04:12:09', 'Trade enriched'),

('2e68899f-7b7c-4284-8195-4b2531491d94', 'trade-committer', 'TRADE_COMMITTED', 'COMMITTED', 'SUCCESS',
 'trade.commit', 'trade.analyze', 'trades.committed', 'cg-commit', 3, 4005, '2026-01-09 04:13:09', 'Trade committed'),

('2e68899f-7b7c-4284-8195-4b2531491d94', 'trade-analyzer', 'TRADE_ANALYZED', 'ANALYZED', 'SUCCESS',
 'trade.analyze', 'none', 'trades.analyzed', 'cg-analyze', 4, 5005, '2026-01-09 04:14:09', 'Trade analyzed');


INSERT INTO rttm_trade_events
(trade_id, service_name, event_type, event_stage, event_status,
 source_queue, target_queue, topic_name, consumer_group, partition_id, offset_value, event_time, message)
VALUES
('9054be3a-2912-4360-808f-6421a44500a6', 'trade-ingest', 'TRADE_RECEIVED', 'RECEIVED', 'SUCCESS',
 'trade.in', 'trade.validate', 'trades.raw', 'cg-ingest', 0, 1006, '2026-01-09 04:11:09', 'Trade received'),

('9054be3a-2912-4360-808f-6421a44500a6', 'trade-validator', 'TRADE_VALIDATED', 'VALIDATED', 'SUCCESS',
 'trade.validate', 'trade.enrich', 'trades.validated', 'cg-validate', 1, 2006, '2026-01-09 04:12:09', 'Trade validated'),

('9054be3a-2912-4360-808f-6421a44500a6', 'trade-enricher', 'TRADE_ENRICHED', 'ENRICHED', 'SUCCESS',
 'trade.enrich', 'trade.commit', 'trades.enriched', 'cg-enrich', 2, 3006, '2026-01-09 04:13:09', 'Trade enriched'),

('9054be3a-2912-4360-808f-6421a44500a6', 'trade-committer', 'TRADE_COMMITTED', 'COMMITTED', 'SUCCESS',
 'trade.commit', 'trade.analyze', 'trades.committed', 'cg-commit', 3, 4006, '2026-01-09 04:14:09', 'Trade committed'),

('9054be3a-2912-4360-808f-6421a44500a6', 'trade-analyzer', 'TRADE_ANALYZED', 'ANALYZED', 'SUCCESS',
 'trade.analyze', 'none', 'trades.analyzed', 'cg-analyze', 4, 5006, '2026-01-09 04:15:09', 'Trade analyzed');


INSERT INTO rttm_trade_events
(trade_id, service_name, event_type, event_stage, event_status,
 source_queue, target_queue, topic_name, consumer_group, partition_id, offset_value, event_time, message)
VALUES
('4970a661-8d82-449a-8aa2-4a39f18786ae', 'trade-ingest', 'TRADE_RECEIVED', 'RECEIVED', 'SUCCESS',
 'trade.in', 'trade.validate', 'trades.raw', 'cg-ingest', 0, 1007, '2026-01-09 04:12:09', 'Trade received'),

('4970a661-8d82-449a-8aa2-4a39f18786ae', 'trade-validator', 'TRADE_VALIDATED', 'VALIDATED', 'SUCCESS',
 'trade.validate', 'trade.enrich', 'trades.validated', 'cg-validate', 1, 2007, '2026-01-09 04:13:09', 'Trade validated'),

('4970a661-8d82-449a-8aa2-4a39f18786ae', 'trade-enricher', 'TRADE_ENRICHED', 'ENRICHED', 'SUCCESS',
 'trade.enrich', 'trade.commit', 'trades.enriched', 'cg-enrich', 2, 3007, '2026-01-09 04:14:09', 'Trade enriched'),

('4970a661-8d82-449a-8aa2-4a39f18786ae', 'trade-committer', 'TRADE_COMMITTED', 'COMMITTED', 'SUCCESS',
 'trade.commit', 'trade.analyze', 'trades.committed', 'cg-commit', 3, 4007, '2026-01-09 04:15:09', 'Trade committed'),

('4970a661-8d82-449a-8aa2-4a39f18786ae', 'trade-analyzer', 'TRADE_ANALYZED', 'ANALYZED', 'SUCCESS',
 'trade.analyze', 'none', 'trades.analyzed', 'cg-analyze', 4, 5007, '2026-01-09 04:16:09', 'Trade analyzed');


INSERT INTO rttm_trade_events
(trade_id, service_name, event_type, event_stage, event_status,
 source_queue, target_queue, topic_name, consumer_group, partition_id, offset_value, event_time, message)
VALUES
('3722ed60-07a8-4f64-b9c0-faf2d00ef194', 'trade-ingest', 'TRADE_RECEIVED', 'RECEIVED', 'SUCCESS',
 'trade.in', 'trade.validate', 'trades.raw', 'cg-ingest', 0, 1008, '2026-01-09 04:13:09', 'Trade received'),

('3722ed60-07a8-4f64-b9c0-faf2d00ef194', 'trade-validator', 'TRADE_VALIDATED', 'VALIDATED', 'SUCCESS',
 'trade.validate', 'trade.enrich', 'trades.validated', 'cg-validate', 1, 2008, '2026-01-09 04:14:09', 'Trade validated'),

('3722ed60-07a8-4f64-b9c0-faf2d00ef194', 'trade-enricher', 'TRADE_ENRICHED', 'ENRICHED', 'SUCCESS',
 'trade.enrich', 'trade.commit', 'trades.enriched', 'cg-enrich', 2, 3008, '2026-01-09 04:15:09', 'Trade enriched'),

('3722ed60-07a8-4f64-b9c0-faf2d00ef194', 'trade-committer', 'TRADE_COMMITTED', 'COMMITTED', 'SUCCESS',
 'trade.commit', 'trade.analyze', 'trades.committed', 'cg-commit', 3, 4008, '2026-01-09 04:16:09', 'Trade committed'),

('3722ed60-07a8-4f64-b9c0-faf2d00ef194', 'trade-analyzer', 'TRADE_ANALYZED', 'ANALYZED', 'SUCCESS',
 'trade.analyze', 'none', 'trades.analyzed', 'cg-analyze', 4, 5008, '2026-01-09 04:17:09', 'Trade analyzed');


INSERT INTO rttm_trade_events
(trade_id, service_name, event_type, event_stage, event_status,
 source_queue, target_queue, topic_name, consumer_group, partition_id, offset_value, event_time, message)
VALUES
('e75447ad-0a05-4c79-b54c-25d5d94d115b', 'trade-ingest', 'TRADE_RECEIVED', 'RECEIVED', 'SUCCESS',
 'trade.in', 'trade.validate', 'trades.raw', 'cg-ingest', 0, 1009, '2026-01-09 04:14:09', 'Trade received'),

('e75447ad-0a05-4c79-b54c-25d5d94d115b', 'trade-validator', 'TRADE_VALIDATED', 'VALIDATED', 'SUCCESS',
 'trade.validate', 'trade.enrich', 'trades.validated', 'cg-validate', 1, 2009, '2026-01-09 04:15:09', 'Trade validated'),

('e75447ad-0a05-4c79-b54c-25d5d94d115b', 'trade-enricher', 'TRADE_ENRICHED', 'ENRICHED', 'SUCCESS',
 'trade.enrich', 'trade.commit', 'trades.enriched', 'cg-enrich', 2, 3009, '2026-01-09 04:16:09', 'Trade enriched'),

('e75447ad-0a05-4c79-b54c-25d5d94d115b', 'trade-committer', 'TRADE_COMMITTED', 'COMMITTED', 'SUCCESS',
 'trade.commit', 'trade.analyze', 'trades.committed', 'cg-commit', 3, 4009, '2026-01-09 04:17:09', 'Trade committed'),

('e75447ad-0a05-4c79-b54c-25d5d94d115b', 'trade-analyzer', 'TRADE_ANALYZED', 'ANALYZED', 'SUCCESS',
 'trade.analyze', 'none', 'trades.analyzed', 'cg-analyze', 4, 5009, '2026-01-09 04:18:09', 'Trade analyzed');


-- rttm_queue_metrics


INSERT INTO rttm_queue_metrics
(service_name, topic_name, partition_id, produced_offset, consumed_offset, consumer_group, snapshot_time)
VALUES
('trade-validator', 'trades.raw', 0, 5000, 4800, 'cg-validate', '2026-01-09 04:30:09');


INSERT INTO rttm_queue_metrics
(service_name, topic_name, partition_id, produced_offset, consumed_offset, consumer_group, snapshot_time)
VALUES
('trade-validator', 'trades.raw', 1, 5100, 4900, 'cg-validate', '2026-01-09 04:30:09');


INSERT INTO rttm_queue_metrics
(service_name, topic_name, partition_id, produced_offset, consumed_offset, consumer_group, snapshot_time)
VALUES
('trade-validator', 'trades.raw', 2, 5200, 5000, 'cg-validate', '2026-01-09 04:30:09');


-- additional queue metric snapshots to create varied partition lags
INSERT INTO rttm_queue_metrics
(service_name, topic_name, partition_id, produced_offset, consumed_offset, consumer_group, snapshot_time)
VALUES
('trade-validator', 'trades.raw', 3, 5300, 4900, 'cg-validate', '2026-01-09 04:30:09'),
('trade-validator', 'trades.raw', 4, 5400, 5400, 'cg-validate', '2026-01-09 04:30:09'),
('trade-ingest',  'trades.raw', 0, 6000, 5950, 'cg-ingest',   '2026-01-09 04:30:09'),
('trade-ingest',  'trades.raw', 1, 6100, 6000, 'cg-ingest',   '2026-01-09 04:30:09');


-- rttm_dlq_events


INSERT INTO rttm_dlq_events
(trade_id, service_name, topic_name, original_topic, reason, event_time, event_stage)
VALUES
('a2c04c8f-aa0b-4f6d-bd56-f103f612fcc1', 'trade-validator', 'trades.dlq', 'trades.raw',
 'Schema validation failed', '2026-01-09 04:23:09', 'VALIDATED');

INSERT INTO rttm_dlq_events
(trade_id, service_name, topic_name, original_topic, reason, event_time, event_stage)
VALUES
('2437b8ca-5d13-428d-b6ea-85fa5647d86e', 'trade-validator', 'trades.dlq', 'trades.raw',
 'Missing mandatory field: instrumentId', '2026-01-09 04:24:10', 'VALIDATED'),

('a5ffcea6-31c6-4c10-977d-aa4965d82902', 'trade-enricher', 'trades.dlq', 'trades.validated',
 'Reference data not found', '2026-01-09 04:26:15', 'ENRICHED'),

('4a4b1542-d438-4d7d-86d3-8119f25566d2', 'trade-committer', 'trades.dlq', 'trades.enriched',
 'Database constraint violation', '2026-01-09 04:28:20', 'COMMITTED'),

('2b871eed-c638-436b-8d17-8f98bd64b1ec', 'trade-validator', 'trades.dlq', 'trades.raw',
 'Invalid trade date format', '2026-01-09 04:29:05', 'VALIDATED'),

('2e68899f-7b7c-4284-8195-4b2531491d94', 'trade-enricher', 'trades.dlq', 'trades.validated',
 'Counterparty enrichment timeout', '2026-01-09 04:30:45', 'ENRICHED');


-- rttm_error_events


INSERT INTO rttm_error_events
(trade_id, service_name, error_type, error_message, event_stage, event_time)
VALUES
('2437b8ca-5d13-428d-b6ea-85fa5647d86e', 'trade-enricher', 'TECHNICAL',
 'Timeout calling enrichment service', 'ENRICHED', '2026-01-09 04:25:09');

INSERT INTO rttm_error_events
(trade_id, service_name, error_type, error_message, event_stage, event_time)
VALUES
('a5ffcea6-31c6-4c10-977d-aa4965d82902', 'trade-validator', 'BUSINESS',
 'Trade amount exceeds allowed limit', 'VALIDATED', '2026-01-09 04:23:30'),

('4a4b1542-d438-4d7d-86d3-8119f25566d2', 'trade-enricher', 'TECHNICAL',
 'Failed to fetch FX rates from cache', 'ENRICHED', '2026-01-09 04:24:55'),

('2b871eed-c638-436b-8d17-8f98bd64b1ec', 'trade-committer', 'TECHNICAL',
 'Deadlock detected while persisting trade', 'COMMITTED', '2026-01-09 04:26:40'),

('2e68899f-7b7c-4284-8195-4b2531491d94', 'trade-analyzer', 'TECHNICAL',
 'Analytics service unavailable', 'ANALYZED', '2026-01-09 04:27:50'),

('9054be3a-2912-4360-808f-6421a44500a6', 'trade-enricher', 'BUSINESS',
 'Unsupported product type for enrichment', 'ENRICHED', '2026-01-09 04:29:10');


-- rttm_alert_thresholds


INSERT INTO rttm_alert_thresholds
(metric_name, service_name, threshold_value, comparison, severity)
VALUES
('TPS', 'none', 1000, '>', 'HIGH'),
('DLQ_COUNT', 'trade-validator', 10, '>', 'CRITICAL'),
('LATENCY_P99', 'trade-enricher', 500, '>', 'HIGH');


-- rttm_alerts


INSERT INTO rttm_alerts
(metric_name, service_name, current_value, threshold_value, severity, triggered_time, status)
VALUES
('LATENCY_P99', 'trade-enricher', 620, 500, 'HIGH', '2026-01-09 04:27:09', 'ACTIVE');

INSERT INTO rttm_alerts
(metric_name, service_name, current_value, threshold_value, severity, triggered_time, status)
VALUES
('DLQ_COUNT', 'trade-validator', 14, 10, 'CRITICAL', '2026-01-09 04:28:30', 'ACTIVE'),

('ERROR_RATE', 'trade-enricher', 7.5, 5.0, 'HIGH', '2026-01-09 04:29:15', 'ACTIVE'),

('QUEUE_LAG', 'trade-validator', 450, 300, 'HIGH', '2026-01-09 04:30:00', 'ACTIVE'),

('LATENCY_P95', 'trade-committer', 380, 300, 'MEDIUM', '2026-01-09 04:31:10', 'ACTIVE'),

('TPS', 'none', 1250, 1000, 'HIGH', '2026-01-09 04:32:00', 'RESOLVED');


INSERT INTO rttm_alerts (metric_name, service_name, current_value, threshold_value, severity, triggered_time, status)
VALUES
('KAFKA_LAG', 'trade-ingest', 1250.0, 1000.0, 'HIGH', '2026-01-15 10:42:00', 'ACTIVE'),
('KAFKA_LAG', 'trade-validator', 820.0, 500.0, 'MEDIUM', '2026-01-15 10:39:30', 'ACTIVE'),
('DLQ_COUNT', 'trade-enricher', 23.0, 5.0, 'HIGH', '2026-01-15 10:40:10', 'ACTIVE'),
('DLQ_COUNT', 'trade-committer', 4.0, 10.0, 'LOW', '2026-01-15 09:58:00', 'RESOLVED'),
('ERROR_RATE', 'trade-analyzer', 0.085, 0.05, 'MEDIUM', '2026-01-15 10:37:55', 'ACTIVE'),
('LATENCY_P99', 'trade-enricher', 230.0, 200.0, 'HIGH', '2026-01-15 10:41:12', 'ACTIVE'),
('TPS', NULL, 650.0, 1200.0, 'MEDIUM', '2026-01-15 10:35:00', 'ACTIVE'),
('LATENCY_P95', 'trade-committer', 120.0, 150.0, 'LOW', '2026-01-15 10:32:00', 'ACKED'),
('KAFKA_LAG', 'kafka-consumer-group-A', 340.0, 300.0, 'MEDIUM', '2026-01-15 10:30:00', 'ACTIVE'),
('ERROR_RATE', 'trade-validator', 0.012, 0.01, 'LOW', '2026-01-15 09:50:45', 'RESOLVED'),
('ANOMALY_DETECTOR_SCORE', NULL, 0.92, 0.8, 'CRITICAL', '2026-01-15 10:43:05', 'ACTIVE'),
('DLQ_SPIKE', 'trade-ingest', 45.0, 10.0, 'CRITICAL', '2026-01-15 10:44:00', 'ACTIVE');

-- rttm_stage_latency


INSERT INTO rttm_stage_latency
(trade_id, service_name, stage_name, latency_ms, event_time)
VALUES
('a2c04c8f-aa0b-4f6d-bd56-f103f612fcc1', 'trade-validator', 'VALIDATED', 120, '2026-01-09 04:20:09'),
('a2c04c8f-aa0b-4f6d-bd56-f103f612fcc1', 'trade-enricher', 'ENRICHED', 240, '2026-01-09 04:21:09'),
('a2c04c8f-aa0b-4f6d-bd56-f103f612fcc1', 'trade-committer', 'COMMITTED', 180, '2026-01-09 04:22:09');


-- add RECEIVED stage latencies so latencyStats for RECEIVED returns values
INSERT INTO rttm_stage_latency
(trade_id, service_name, stage_name, latency_ms, event_time)
VALUES
('00000000-0000-0000-0000-000000000001', 'trade-ingest', 'RECEIVED', 95,  '2026-01-09 04:18:09'),
('00000000-0000-0000-0000-000000000002', 'trade-ingest', 'RECEIVED', 110, '2026-01-09 04:18:29'),
('00000000-0000-0000-0000-000000000003', 'trade-ingest', 'RECEIVED', 85,  '2026-01-09 04:18:49'),
('00000000-0000-0000-0000-000000000004', 'trade-ingest', 'RECEIVED', 130, '2026-01-09 04:19:09');


INSERT INTO rttm_stage_latency
(trade_id, service_name, stage_name, latency_ms, event_time)
VALUES
('2437b8ca-5d13-428d-b6ea-85fa5647d86e', 'trade-validator', 'VALIDATED', 120, '2026-01-09 04:20:09'),
('2437b8ca-5d13-428d-b6ea-85fa5647d86e', 'trade-enricher', 'ENRICHED', 240, '2026-01-09 04:21:09'),
('2437b8ca-5d13-428d-b6ea-85fa5647d86e', 'trade-committer', 'COMMITTED', 180, '2026-01-09 04:22:09');


INSERT INTO rttm_stage_latency
(trade_id, service_name, stage_name, latency_ms, event_time)
VALUES
('a5ffcea6-31c6-4c10-977d-aa4965d82902', 'trade-validator', 'VALIDATED', 120, '2026-01-09 04:20:09'),
('a5ffcea6-31c6-4c10-977d-aa4965d82902', 'trade-enricher', 'ENRICHED', 240, '2026-01-09 04:21:09'),
('a5ffcea6-31c6-4c10-977d-aa4965d82902', 'trade-committer', 'COMMITTED', 180, '2026-01-09 04:22:09');


INSERT INTO rttm_stage_latency
(trade_id, service_name, stage_name, latency_ms, event_time)
VALUES
('4a4b1542-d438-4d7d-86d3-8119f25566d2', 'trade-validator', 'VALIDATED', 120, '2026-01-09 04:20:09'),
('4a4b1542-d438-4d7d-86d3-8119f25566d2', 'trade-enricher', 'ENRICHED', 240, '2026-01-09 04:21:09'),
('4a4b1542-d438-4d7d-86d3-8119f25566d2', 'trade-committer', 'COMMITTED', 180, '2026-01-09 04:22:09');


INSERT INTO rttm_stage_latency
(trade_id, service_name, stage_name, latency_ms, event_time)
VALUES
('2b871eed-c638-436b-8d17-8f98bd64b1ec', 'trade-validator', 'VALIDATED', 120, '2026-01-09 04:20:09'),
('2b871eed-c638-436b-8d17-8f98bd64b1ec', 'trade-enricher', 'ENRICHED', 240, '2026-01-09 04:21:09'),
('2b871eed-c638-436b-8d17-8f98bd64b1ec', 'trade-committer', 'COMMITTED', 180, '2026-01-09 04:22:09');


INSERT INTO rttm_stage_latency
(trade_id, service_name, stage_name, latency_ms, event_time)
VALUES
('2e68899f-7b7c-4284-8195-4b2531491d94', 'trade-validator', 'VALIDATED', 120, '2026-01-09 04:20:09'),
('2e68899f-7b7c-4284-8195-4b2531491d94', 'trade-enricher', 'ENRICHED', 240, '2026-01-09 04:21:09'),
('2e68899f-7b7c-4284-8195-4b2531491d94', 'trade-committer', 'COMMITTED', 180, '2026-01-09 04:22:09');


INSERT INTO rttm_stage_latency
(trade_id, service_name, stage_name, latency_ms, event_time)
VALUES
('9054be3a-2912-4360-808f-6421a44500a6', 'trade-validator', 'VALIDATED', 120, '2026-01-09 04:20:09'),
('9054be3a-2912-4360-808f-6421a44500a6', 'trade-enricher', 'ENRICHED', 240, '2026-01-09 04:21:09'),
('9054be3a-2912-4360-808f-6421a44500a6', 'trade-committer', 'COMMITTED', 180, '2026-01-09 04:22:09');


INSERT INTO rttm_stage_latency
(trade_id, service_name, stage_name, latency_ms, event_time)
VALUES
('4970a661-8d82-449a-8aa2-4a39f18786ae', 'trade-validator', 'VALIDATED', 120, '2026-01-09 04:20:09'),
('4970a661-8d82-449a-8aa2-4a39f18786ae', 'trade-enricher', 'ENRICHED', 240, '2026-01-09 04:21:09'),
('4970a661-8d82-449a-8aa2-4a39f18786ae', 'trade-committer', 'COMMITTED', 180, '2026-01-09 04:22:09');


INSERT INTO rttm_stage_latency
(trade_id, service_name, stage_name, latency_ms, event_time)
VALUES
('3722ed60-07a8-4f64-b9c0-faf2d00ef194', 'trade-validator', 'VALIDATED', 120, '2026-01-09 04:20:09'),
('3722ed60-07a8-4f64-b9c0-faf2d00ef194', 'trade-enricher', 'ENRICHED', 240, '2026-01-09 04:21:09'),
('3722ed60-07a8-4f64-b9c0-faf2d00ef194', 'trade-committer', 'COMMITTED', 180, '2026-01-09 04:22:09');


INSERT INTO rttm_stage_latency
(trade_id, service_name, stage_name, latency_ms, event_time)
VALUES
('e75447ad-0a05-4c79-b54c-25d5d94d115b', 'trade-validator', 'VALIDATED', 120, '2026-01-09 04:20:09'),
('e75447ad-0a05-4c79-b54c-25d5d94d115b', 'trade-enricher', 'ENRICHED', 240, '2026-01-09 04:21:09'),
('e75447ad-0a05-4c79-b54c-25d5d94d115b', 'trade-committer', 'COMMITTED', 180, '2026-01-09 04:22:09');


-- additional received trade events to increase TPS counts in recent minutes
INSERT INTO rttm_trade_events
(trade_id, service_name, event_type, event_stage, event_status,
 source_queue, target_queue, topic_name, consumer_group, partition_id, offset_value, event_time, message)
VALUES
('11111111-1111-1111-1111-111111111111', 'trade-ingest', 'TRADE_RECEIVED', 'RECEIVED', 'SUCCESS', 'trade.in', 'trade.validate', 'trades.raw', 'cg-ingest', 0, 1010, '2026-01-09 04:20:10', 'Trade received'),
('22222222-2222-2222-2222-222222222222', 'trade-ingest', 'TRADE_RECEIVED', 'RECEIVED', 'SUCCESS', 'trade.in', 'trade.validate', 'trades.raw', 'cg-ingest', 1, 1011, '2026-01-09 04:20:11', 'Trade received'),
('33333333-3333-3333-3333-333333333333', 'trade-ingest', 'TRADE_RECEIVED', 'RECEIVED', 'SUCCESS', 'trade.in', 'trade.validate', 'trades.raw', 'cg-ingest', 2, 1012, '2026-01-09 04:20:12', 'Trade received'),
('44444444-4444-4444-4444-444444444444', 'trade-ingest', 'TRADE_RECEIVED', 'RECEIVED', 'SUCCESS', 'trade.in', 'trade.validate', 'trades.raw', 'cg-ingest', 0, 1013, '2026-01-09 04:20:13', 'Trade received'),
('55555555-5555-5555-5555-555555555555', 'trade-ingest', 'TRADE_RECEIVED', 'RECEIVED', 'SUCCESS', 'trade.in', 'trade.validate', 'trades.raw', 'cg-ingest', 1, 1014, '2026-01-09 04:20:14', 'Trade received');
