# Stage Latency & Alert Generation Implementation Summary

## Overview
Implemented automatic stage latency computation and alert generation for the RTTM service.

## What Was Created

### 1. **StageLatencyComputationService** ([service/StageLatencyComputationService.java](src/main/java/com/pms/rttm/service/StageLatencyComputationService.java))
Computes latencies between consecutive pipeline stages for each trade.

**How it works:**
- Triggered automatically after each batch of trade events is saved
- For each unique trade ID, fetches all events ordered by time
- Calculates time difference between consecutive stages (RECEIVED → VALIDATED → ENRICHED → COMMITTED → ANALYZED)
- Saves latency records to `rttm_stage_latency` table

**Key methods:**
- `computeAndSaveLatencies(UUID tradeId)` - Compute latencies for a single trade
- `computeAndSaveLatenciesBatch(Collection<UUID> tradeIds)` - Batch computation for multiple trades

### 2. **AlertGenerationService** ([service/AlertGenerationService.java](src/main/java/com/pms/rttm/service/AlertGenerationService.java))
Monitors metrics and generates alerts when thresholds are exceeded.

**Monitored Metrics:**
1. **Stage Latencies** - Average latency per stage (WARNING: 1s, CRITICAL: 3s)
2. **Error Rates** - Error count in last 5 minutes (WARNING: 5, CRITICAL: 20)
3. **DLQ Counts** - DLQ event count in last 5 minutes (WARNING: 10, CRITICAL: 50)
4. **Queue Depths** - Maximum queue lag (WARNING: 1000, CRITICAL: 5000)
5. **Peak TPS** - Transactions per second in last 60s (WARNING: 10k, CRITICAL: 15k)

**Key methods:**
- `evaluateAndGenerateAlerts()` - Evaluate all conditions and create alerts
- `resolveAlert(Long alertId)` - Manually resolve an alert
- `autoResolveStaleAlerts(int ageMinutes)` - Auto-resolve old alerts

### 3. **AlertScheduler** ([service/AlertScheduler.java](src/main/java/com/pms/rttm/service/AlertScheduler.java))
Background scheduler for periodic alert evaluation.

**Scheduled Tasks:**
- Alert evaluation: Every 60 seconds (configurable)
- Auto-resolve stale alerts: Every 5 minutes (configurable)

### 4. **Updated Components**

#### RttmIngestService ([service/RttmIngestService.java](src/main/java/com/pms/rttm/service/RttmIngestService.java))
Added repositories and methods for:
- `RttmStageLatencyEntity` ingestion
- `RttmAlertEntity` ingestion

#### BatchQueueService ([service/BatchQueueService.java](src/main/java/com/pms/rttm/service/BatchQueueService.java))
Enhanced `processTradeBatch()` to:
- Collect unique trade IDs from each batch
- Automatically compute stage latencies after saving trade events
- Log latency computation results

#### Repository Updates
Added missing query methods:
- [RttmErrorEventRepository.java](src/main/java/com/pms/rttm/repository/RttmErrorEventRepository.java): `countByEventTimeAfter(Instant)`
- [RttmDlqEventRepository.java](src/main/java/com/pms/rttm/repository/RttmDlqEventRepository.java): `countByEventTimeAfter(Instant)`
- [RttmQueueMetricRepository.java](src/main/java/com/pms/rttm/repository/RttmQueueMetricRepository.java): `findMaxQueueDepthSince(Instant)`

### 5. **Configuration** ([application.yml](src/main/resources/application.yml))
Added comprehensive alert configuration section:

```yaml
rttm:
  alerts:
    evaluation-interval-ms: 60000        # Alert evaluation frequency
    auto-resolve-interval-ms: 300000     # Auto-resolve frequency
    
    latency:
      warning.ms: 1000                   # 1 second
      critical.ms: 3000                  # 3 seconds
    
    error-rate:
      warning: 5                         # 5 errors in 5 minutes
      critical: 20                       # 20 errors in 5 minutes
    
    dlq:
      warning: 10                        # 10 DLQ events in 5 minutes
      critical: 50                       # 50 DLQ events in 5 minutes
    
    queue-depth:
      warning: 1000
      critical: 5000
    
    tps:
      warning: 10000
      critical: 15000
```

All thresholds are configurable via environment variables.

## Data Flow

### Stage Latency Computation
```
Trade Events → Kafka Consumer → BatchQueueService.processTradeBatch()
    ↓
Save to rttm_trade_events table
    ↓
StageLatencyComputationService.computeAndSaveLatenciesBatch()
    ↓
Calculate stage-to-stage latencies
    ↓
Save to rttm_stage_latency table
```

### Alert Generation
```
Scheduled Task (every 60s) → AlertScheduler.evaluateAlerts()
    ↓
AlertGenerationService.evaluateAndGenerateAlerts()
    ↓
Check multiple metrics:
  - Stage latencies (from rttm_stage_latency)
  - Error rates (from rttm_error_events)
  - DLQ counts (from rttm_dlq_events)
  - Queue depths (from rttm_queue_metrics)
  - Peak TPS (from rttm_trade_events)
    ↓
Generate alerts if thresholds exceeded
    ↓
Save to rttm_alerts table with status='ACTIVE'
```

## How to Build & Run

1. **Build the project** (generates protobuf classes):
   ```bash
   mvn clean package
   ```

2. **Run the application**:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
   ```

3. **Customize alert thresholds** via environment variables:
   ```bash
   export RTTM_ALERT_LATENCY_WARNING=2000
   export RTTM_ALERT_LATENCY_CRITICAL=5000
   export RTTM_ALERT_ERROR_RATE_WARNING=10
   ```

## Testing

The implementation will:
1. ✅ Automatically compute latencies when trade events arrive
2. ✅ Generate alerts every minute based on configured thresholds
3. ✅ Auto-resolve stale alerts older than 15 minutes every 5 minutes
4. ✅ Log all activities for monitoring

You can verify by:
- Checking `rttm_stage_latency` table for computed latencies
- Checking `rttm_alerts` table for generated alerts
- Monitoring application logs for computation and alert generation activities

## Customization

### Adjust Alert Thresholds
Edit [application.yml](src/main/resources/application.yml) or set environment variables.

### Add Custom Alert Types
1. Add new check method in `AlertGenerationService`
2. Call it from `evaluateAndGenerateAlerts()`
3. Add threshold configuration to `application.yml`

### Change Evaluation Frequency
Modify `rttm.alerts.evaluation-interval-ms` in configuration (default: 60000ms = 1 minute)

## Notes

- Stage latency is computed **per trade ID** based on event progression
- Alerts are generated with severity levels: `WARNING` or `CRITICAL`
- Alert status can be: `ACTIVE`, `RESOLVED`, or `AUTO_RESOLVED`
- All database operations are transactional for data consistency
- The implementation handles errors gracefully and logs issues without crashing
