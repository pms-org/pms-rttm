# Stage Latency Computation Logic

## Overview
The `StageLatencyComputationService` computes latencies between consecutive pipeline stages for trade events. It handles partial/incomplete trade flows correctly by computing latencies for whatever consecutive stage pairs exist.

## Workflow for Partial Pipeline Example

**Scenario:** Trade receives events for RECEIVED → VALIDATED → ENRICHED (but not COMMITTED)

### 1. Fetch and Prepare Events
```java
// Gets all events for the trade, reverses to chronological order
List<RttmTradeEventEntity> events = [...]; // RECEIVED, VALIDATED, ENRICHED
Collections.reverse(events);
```

### 2. Build Stage Map (First Occurrence of Each Stage)
```java
Map<EventStage, RttmTradeEventEntity> stageMap = {
    RECEIVED → event1,
    VALIDATED → event2,
    ENRICHED → event3
    // COMMITTED → null (not present)
}
```

### 3. Loop Through Consecutive Stage Pairs
```java
for (int i = 0; i < STAGE_ORDER.size() - 1; i++) // i goes 0, 1, 2
```

| i | currentStage | nextStage | currentEvent | nextEvent | Latency Computed? |
|---|-------------|-----------|--------------|-----------|-------------------|
| 0 | RECEIVED | VALIDATED | ✓ exists | ✓ exists | **YES** - computes latency |
| 1 | VALIDATED | ENRICHED | ✓ exists | ✓ exists | **YES** - computes latency |
| 2 | ENRICHED | COMMITTED | ✓ exists | ❌ null | **NO** - skipped (both must exist) |

### 4. Result
Creates 2 latency records:
1. **VALIDATED** stage latency (time from RECEIVED to VALIDATED)
2. **ENRICHED** stage latency (time from VALIDATED to ENRICHED)

Both saved to `rttm_stage_latency` table.

## Key Logic Points

### Consecutive Pair Requirement
The `if (currentEvent != null && nextEvent != null)` check means:
- Both stages in a pair must exist to compute latency
- Partial pipelines work fine - you get latencies for whatever consecutive pairs exist
- If a trade only reaches VALIDATED, you'd get 1 latency (RECEIVED→VALIDATED)
- Gaps don't break earlier computations

### Example Scenarios

| Stages Present | Latencies Computed |
|---------------|-------------------|
| RECEIVED only | 0 (no consecutive pair) |
| RECEIVED, VALIDATED | 1 (RECEIVED→VALIDATED) |
| RECEIVED, VALIDATED, ENRICHED | 2 (RECEIVED→VALIDATED, VALIDATED→ENRICHED) |
| RECEIVED, ENRICHED (gap) | 0 (not consecutive) |
| All 4 stages | 3 (all consecutive pairs) |

### Warning Condition
```java
log.warn("No latencies computed for trade {} - stages found: {} (need consecutive stages)",
```
This warning only appears when NO consecutive pairs exist (e.g., only RECEIVED, or only ENRICHED with no adjacent stages).

## Batch Processing
The `computeAndSaveLatenciesBatch()` method:
- Processes multiple trade IDs in one transaction
- Continues on error (logs failure, doesn't abort entire batch)
- Returns total count of latencies computed across all trades
- Useful for bulk processing after batch ingestion in `BatchQueueService`

## Trade Flow Support
**Partial/incomplete trade flows are handled correctly** - you get incremental latency metrics as far as the trade progressed through the pipeline. This is important for:
- Identifying where trades get stuck
- Computing SLA metrics for each stage independently
- Observability when pipelines are partially down
