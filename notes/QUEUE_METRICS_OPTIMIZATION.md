# Queue Metrics Optimization Implementation

## Problem
Queue metrics arrive every 30 seconds from each service for every topic/partition they consume, leading to exponential database growth. For example, with 5 services × 4 topics × 6 partitions = 120 records every 30s = 240,960 records/day.

## Solution Implemented

### 1. **Aggregation (Option 2)** - Reduce Ingestion Rate
**File:** `QueueMetricAggregationService.java`

- **Only stores metrics when:**
  - Lag changes by >10% (configurable via `rttm.queue-metrics.aggregation-threshold`)
  - OR 5 minutes elapsed since last storage (configurable via `rttm.queue-metrics.min-interval-minutes`)
  
- **Expected reduction:** 80-90% fewer records stored
- **Benefit:** No loss of critical data (spikes/changes are captured), stable periods skip redundant snapshots

### 2. **Retention Cleanup (Option 1)** - Auto-Delete Old Data
**File:** `QueueMetricCleanupService.java`

- **Retention window:** 48 hours (configurable via `rttm.queue-metrics.retention-hours`)
- **Cleanup schedule:** Daily at 2 AM (configurable via `rttm.queue-metrics.cleanup-cron`)
- **Benefit:** Database size remains bounded regardless of throughput

## Files Modified

### New Services
1. **`QueueMetricAggregationService.java`** - Intelligent filtering before storage
2. **`QueueMetricCleanupService.java`** - Scheduled cleanup of old metrics

### Updated Files
1. **`RttmQueueMetricRepository.java`** - Added `deleteBySnapshotTimeBefore()` method
2. **`BatchQueueService.java`** - Integrated aggregation service in `processMetricBatch()`
3. **`application.yml`** - Added queue-metrics configuration section

## Configuration

Add to `application.yml` (already added with defaults):

```yaml
rttm:
  queue-metrics:
    retention-hours: 48                      # Keep last 48 hours only
    cleanup-cron: "0 0 2 * * ?"              # Daily cleanup at 2 AM
    aggregation-threshold: 0.1               # Store if lag changes >10%
    min-interval-minutes: 5                  # Or 5 minutes elapsed
```

## Environment Variable Overrides

```bash
# Override retention (hours)
RTTM_QUEUE_METRICS_RETENTION_HOURS=24

# Override cleanup schedule (cron)
RTTM_QUEUE_METRICS_CLEANUP_CRON="0 0 3 * * ?"

# Override aggregation threshold (0.1 = 10%)
RTTM_QUEUE_METRICS_AGG_THRESHOLD=0.15

# Override min interval (minutes)
RTTM_QUEUE_METRICS_MIN_INTERVAL=10
```

## Expected Impact

### Before Optimization
- **Ingestion:** ~240,960 records/day (5 services × 4 topics × 6 partitions × 2 snapshots/min)
- **Storage:** Growing indefinitely
- **DB size:** ~7.2M records/month

### After Optimization
- **Ingestion:** ~24,000-48,000 records/day (80-90% reduction via aggregation)
- **Storage:** Max 48 hours of data
- **DB size:** ~96,000 records max (stable)
- **Overall reduction:** ~98.6% storage reduction

## How It Works

### Aggregation Flow
1. Kafka consumer receives metric → enqueues to `metricQueue`
2. `BatchQueueService.processMetricBatch()` drains queue
3. For each metric, calls `aggregationService.shouldStore()`
4. Aggregation service checks:
   - Is this first metric for this topic/partition/group? → Store
   - Did lag change >10%? → Store
   - Has 5 minutes elapsed? → Store
   - Otherwise → Skip
5. Only filtered metrics are saved to database

### Cleanup Flow
1. Scheduler triggers daily at 2 AM
2. `QueueMetricCleanupService.cleanupOldMetrics()` executes
3. Calculates cutoff: `now - 48 hours`
4. Deletes all records with `snapshot_time < cutoff`
5. Logs count of deleted records

## Monitoring

Add endpoints to monitor the optimization:

```java
// Check aggregation cache size
aggregationService.getCacheSize()

// Check current metric count
cleanupService.getCurrentMetricCount()
```

## Production Tuning

For production, consider adjusting:

- **Higher retention** for compliance: `retention-hours: 168` (7 days)
- **Stricter aggregation** for high volume: `aggregation-threshold: 0.2` (20% change)
- **Longer intervals** for stable systems: `min-interval-minutes: 10`

## Verification

After deployment, monitor:
1. Log output showing filtered metrics count
2. Database table size growth rate
3. Cleanup job execution logs
4. No missing critical lag spike events

## Rollback

To disable optimization:

```yaml
rttm:
  queue-metrics:
    aggregation-threshold: 0.0    # Store everything (0% threshold)
    retention-hours: 99999        # Never delete
```
