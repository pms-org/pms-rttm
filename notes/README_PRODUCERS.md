# Trade Event Producers

## Overview

Three producer tools for testing RTTM latency computation and alert generation:

1. **TradeProducer** - Sends complete trade lifecycles with realistic latencies
2. **AlertTestProducer** - High-volume burst producer to trigger alerts
3. **Run Commands** - How to execute each producer

---

## 1. TradeProducer

Sends complete trade lifecycle events (RECEIVED → VALIDATED → ENRICHED → COMMITTED → ANALYZED) with realistic stage latencies.

### Features
- Uses valid portfolio IDs from validation service
- Uses valid stock symbols (AAPL, MSFT, GOOGL, AMZN, META, NVDA, NFLX, AMD, INTC, IBM, ORCL, BAC, JPM, WMT)
- Sends all 5 stages for each trade
- Stage latencies: 50-300ms (will trigger latency alerts with current thresholds)
- Perfect for testing stage latency computation

### Usage

```bash
# Set environment variables
set KAFKA_BOOTSTRAP=localhost:9092
set SCHEMA_REGISTRY_URL=http://localhost:8081

# Send 5 complete trades (default)
mvn compile exec:java -Dexec.mainClass=com.pms.rttm.tools.TradeProducer

# Send 20 complete trades
mvn compile exec:java -Dexec.mainClass=com.pms.rttm.tools.TradeProducer -Dexec.args="20"

# Send 100 complete trades
mvn compile exec:java -Dexec.mainClass=com.pms.rttm.tools.TradeProducer -Dexec.args="100"
```

### Expected Output

```
Sending 5 complete trade lifecycles (each with 5 stages)...

Trade #1: a7b3c4d5 (Portfolio: b23d70cf, Stock: AAPL)
  ✓ RECEIVED (latency: 82ms) => offset=1000
  ✓ VALIDATED (latency: 143ms) => offset=1001
  ✓ ENRICHED (latency: 210ms) => offset=1002
  ✓ COMMITTED (latency: 95ms) => offset=1003
  ✓ ANALYZED (latency: 167ms) => offset=1004

Trade #2: e8f9a0b1 (Portfolio: 7839ee86, Stock: MSFT)
  ...

✅ Sent 5 trades × 5 stages = 25 total events
```

### What Gets Tested
- ✅ Stage latency computation (RECEIVED→VALIDATED, VALIDATED→ENRICHED, etc.)
- ✅ Latency alerts (thresholds: 50ms warning, 200ms critical)
- ✅ Trade event ingestion and storage
- ✅ Multi-stage trade tracking

---

## 2. AlertTestProducer

High-volume burst producer designed to trigger ALL alert types.

### Features
- Sends 100+ trades in rapid succession
- NO delays between sends → triggers TPS alerts
- High latencies (100-400ms) → triggers latency alerts
- Burst mode → may trigger queue depth alerts
- Uses valid portfolio IDs and stock symbols

### Usage

```bash
# Set environment variables
set KAFKA_BOOTSTRAP=localhost:9092
set SCHEMA_REGISTRY_URL=http://localhost:8081

# Send 100 trades in burst mode (default)
mvn compile exec:java -Dexec.mainClass=com.pms.rttm.tools.AlertTestProducer

# Send 50 trades
mvn compile exec:java -Dexec.mainClass=com.pms.rttm.tools.AlertTestProducer -Dexec.args="50"

# Send 500 trades (stress test)
mvn compile exec:java -Dexec.mainClass=com.pms.rttm.tools.AlertTestProducer -Dexec.args="500"
```

### Expected Output

```
🔥 ALERT TEST MODE - Sending 100 trades in RAPID BURST to trigger alerts...
Expected alerts:
  - TPS alert (current threshold: 10 TPS)
  - Latency alert (current threshold: 50ms warning / 200ms critical)
  - Queue depth alert (if consuming can't keep up)

  Sent 10 trades (50 events)...
  Sent 20 trades (100 events)...
  ...
  Sent 100 trades (500 events)...

✅ Burst complete:
   - Sent: 100 trades × 5 stages = 500 total events
   - Duration: 1247 ms
   - Throughput: 401.0 TPS

🔔 Check RTTM logs for alerts in ~60 seconds!
```

### What Gets Tested
- ✅ TPS alerts (threshold: 10 TPS → will trigger with 400+ TPS)
- ✅ Latency alerts (100-400ms latencies)
- ✅ Queue depth alerts (burst may overwhelm consumer)
- ✅ Stage latency computation under load
- ✅ Alert generation every 60 seconds

---

## 3. Monitoring Test Results

### Check RTTM Logs

After running producers, watch RTTM logs for:

```
# Latency computation
INFO  c.p.r.s.StageLatencyComputationService - Computing latencies for batch of 5 trades
INFO  c.p.r.s.StageLatencyComputationService - ✓ Computed 20 total latencies for 5 trades

# Alert evaluation (runs every 60s)
INFO  c.p.r.service.AlertGenerationService - Running alert evaluation...
INFO  c.p.r.service.AlertGenerationService - Generated 3 alerts

# Or no alerts if within thresholds
INFO  c.p.r.service.AlertGenerationService - No alerts triggered - all metrics within thresholds
```

### Query Database

```sql
-- Check trade events
SELECT trade_id, event_stage, event_time, service_name 
FROM rttm_trade_events 
ORDER BY event_time DESC LIMIT 50;

-- Check computed latencies
SELECT trade_id, stage_name, latency_ms, event_time
FROM rttm_stage_latency 
ORDER BY event_time DESC LIMIT 50;

-- Check generated alerts
SELECT metric_name, service_name, current_value, threshold_value, severity, triggered_time
FROM rttm_alerts 
WHERE status = 'ACTIVE'
ORDER BY triggered_time DESC;

-- Check average latencies by stage
SELECT stage_name, 
       AVG(latency_ms) as avg_latency,
       MAX(latency_ms) as max_latency,
       COUNT(*) as count
FROM rttm_stage_latency 
WHERE event_time > NOW() - INTERVAL '10 minutes'
GROUP BY stage_name;
```

---

## Current Alert Thresholds (DEV/TEST)

### Latency
- Warning: 50ms
- Critical: 200ms

### TPS
- Warning: 10 TPS
- Critical: 50 TPS

### Error Rate (in 5 min)
- Warning: 1 error
- Critical: 2 errors

### DLQ Count (in 5 min)
- Warning: 1 event
- Critical: 2 events

### Queue Depth
- Warning: 10 messages
- Critical: 50 messages

---

## Testing Workflow

### 1. Normal Testing (Latency Computation)
```bash
# Send 10 trades with realistic latencies
mvn compile exec:java -Dexec.mainClass=com.pms.rttm.tools.TradeProducer -Dexec.args="10"

# Wait 2-3 seconds for ingestion
# Check logs for latency computation
# Query database for stage_latency records
```

### 2. Alert Testing (Trigger All Alerts)
```bash
# Send high-volume burst
mvn compile exec:java -Dexec.mainClass=com.pms.rttm.tools.AlertTestProducer -Dexec.args="100"

# Wait 60 seconds for alert evaluation
# Check logs for alert generation
# Query database for active alerts
```

### 3. Continuous Testing
```bash
# Run TradeProducer every 30 seconds
while ($true) { 
    mvn compile exec:java -Dexec.mainClass=com.pms.rttm.tools.TradeProducer -Dexec.args="5"
    Start-Sleep -Seconds 30 
}
```

---

## Production Reminder

Before deploying to production:
1. Restore production thresholds (see TODO.txt)
2. Use environment variables to override defaults
3. Remove or disable AlertTestProducer (dev/test only)
4. Adjust TradeProducer latencies to realistic production values

---

**Last Updated:** January 30, 2026
