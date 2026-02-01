# Testing & Monitoring Guide

## 1. Running Tests

### Run all tests:
```powershell
mvn test
```

### Run specific test class:
```powershell
mvn test -Dtest=StageLatencyComputationServiceTest
mvn test -Dtest=AlertGenerationServiceTest
```

### Run with coverage:
```powershell
mvn clean test jacoco:report
```

## 2. Monitoring Services When Running with Validation Service

### Step 1: Start All Containers
```powershell
# Start all services including validation
docker-compose up -d

# Or if using single node setup
docker-compose -f docker-compose.single.yml up -d
```

### Step 2: Start RTTM Service
```powershell
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Step 3: Monitor Application Logs

Watch for these key log messages:

#### Trade Event Processing:
```
INFO  BatchQueueService - Saved 100 trades
DEBUG StageLatencyComputationService - Computed 3 latencies for trade <uuid>
```

#### Alert Generation (every 60 seconds):
```
INFO  AlertGenerationService - Generated 2 alerts
```

#### Alert Auto-Resolution (every 5 minutes):
```
INFO  AlertGenerationService - Auto-resolved 3 alerts older than 15 min
```

### Step 4: Query Database to Verify Data

#### Check Trade Events:
```sql
SELECT COUNT(*) FROM rttm_trade_events;
SELECT service_name, event_stage, COUNT(*) 
FROM rttm_trade_events 
GROUP BY service_name, event_stage 
ORDER BY service_name, event_stage;
```

#### Check Stage Latencies (should appear after trades):
```sql
SELECT COUNT(*) FROM rttm_stage_latency;

-- View recent latencies
SELECT 
    trade_id, 
    service_name, 
    stage_name, 
    latency_ms, 
    event_time 
FROM rttm_stage_latency 
ORDER BY event_time DESC 
LIMIT 20;

-- Average latency per stage
SELECT 
    stage_name, 
    AVG(latency_ms) as avg_ms, 
    MAX(latency_ms) as max_ms, 
    COUNT(*) as count 
FROM rttm_stage_latency 
GROUP BY stage_name;
```

#### Check Alerts (should appear every 60 seconds if thresholds exceeded):
```sql
SELECT COUNT(*) FROM rttm_alerts;

-- View active alerts
SELECT 
    id,
    metric_name, 
    service_name, 
    current_value, 
    threshold_value, 
    severity, 
    triggered_time, 
    status 
FROM rttm_alerts 
WHERE status = 'ACTIVE'
ORDER BY triggered_time DESC;

-- Alert summary by metric
SELECT 
    metric_name, 
    severity, 
    status, 
    COUNT(*) 
FROM rttm_alerts 
GROUP BY metric_name, severity, status;
```

### Step 5: Verify End-to-End Flow

When validation service sends trade events, you should see:

1. **Immediate (< 2 seconds):**
   - Trade events consumed from Kafka
   - Saved to `rttm_trade_events` table
   - Stage latencies computed and saved to `rttm_stage_latency` table

2. **Every 60 seconds:**
   - Alert evaluation runs
   - New alerts created if thresholds exceeded
   - Saved to `rttm_alerts` table

3. **Every 5 minutes:**
   - Stale alerts (>15 min old) auto-resolved

### Step 6: Test Alert Generation

You can manually verify alerts by checking if latencies exceed thresholds:

```sql
-- Check if any stage has high average latency (> 1000ms)
SELECT 
    stage_name, 
    AVG(latency_ms) as avg_latency
FROM rttm_stage_latency 
WHERE event_time >= NOW() - INTERVAL '5 minutes'
GROUP BY stage_name 
HAVING AVG(latency_ms) > 1000;
```

If avg > 1000ms, you should see a `LATENCY_P95` alert with severity `HIGH`.
If avg > 3000ms, you should see a `LATENCY_P99` alert with severity `CRITICAL`.

### Step 7: Monitor Kafka Consumer Lag

Check if RTTM is keeping up with incoming events:

```powershell
# Inside Kafka container
docker exec -it <kafka-container> bash

kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group <RTTM_CONSUMER_GROUP> --describe
```

Look for `LAG` column - should be low (< 100) in steady state.

## 3. Troubleshooting

### No latencies being computed?
- Check if trade events are arriving: `SELECT COUNT(*) FROM rttm_trade_events;`
- Check logs for errors in `StageLatencyComputationService`
- Verify trades have multiple stages (not just RECEIVED)

### No alerts being generated?
- Check if alert scheduler is running: Look for log messages every 60s
- Verify metric values exceed thresholds
- Check alert configuration in `application.yml`

### Alerts not auto-resolving?
- Wait 5+ minutes for scheduler to run
- Check if alerts are older than 15 minutes
- Verify scheduler logs

## 4. Sample Queries for Verification

### Complete trade journey:
```sql
SELECT 
    t.trade_id,
    t.event_stage,
    t.event_time,
    l.latency_ms
FROM rttm_trade_events t
LEFT JOIN rttm_stage_latency l ON t.trade_id = l.trade_id AND t.event_stage = l.stage_name
WHERE t.trade_id = '<specific-uuid>'
ORDER BY t.event_time;
```

### Alert statistics for last hour:
```sql
SELECT 
    metric_name,
    COUNT(*) as alert_count,
    AVG(current_value) as avg_value,
    MAX(current_value) as max_value
FROM rttm_alerts
WHERE triggered_time >= NOW() - INTERVAL '1 hour'
GROUP BY metric_name;
```

## 5. Expected Behavior

With validation service running and sending events:

- **Trade events**: Arriving continuously
- **Latencies**: Computed within 2 seconds of trade events
- **Alerts**: Generated every 60 seconds if thresholds exceeded
- **Database growth**: 
  - `rttm_trade_events`: Grows continuously
  - `rttm_stage_latency`: ~3 records per trade (VALIDATED, ENRICHED, COMMITTED)
  - `rttm_alerts`: New records every 60s when metrics breach thresholds

## 6. Adjusting Thresholds

If you want to see alerts immediately (for testing), lower the thresholds:

In `application.yml` or environment variables:
```yaml
rttm:
  alerts:
    latency:
      warning.ms: 100      # Lower to trigger on any latency
      critical.ms: 500
    error-rate:
      warning: 1           # Trigger on single error
      critical: 5
```

Then restart the application to see alerts generate quickly.
