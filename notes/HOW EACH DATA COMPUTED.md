
# RTTM Metrics Computation Reference

## Top-Level Metrics (Last 24h data)

### 1. Current TPS (Transactions Per Second)

**Logic:** Count the number of trades at the RECEIVED stage in the last 1 second.

This metric shows how many trades the Trade-capture service received in the most recent second, providing real-time insight into system throughput.

**Example:**
```
If in the last second (from 10:30:45 to 10:30:46):
- 5 trades arrived at RECEIVED stage
Then Current TPS = 5 tx/s
```

---

### 2. Peak TPS

**Logic:** Find the maximum number of trades that occurred in any single second within the last 24 hours that reached RECEIVED stage.

The system groups all trades of RECEIVED stage from the last 24 hours by second, counts how many trades occurred in each second, and returns the highest count.

**Example:**
```
Last 24h data by second:
- 10:00:00 → 10 trades
- 10:00:01 → 50 trades  ← Peak
- 10:00:02 → 30 trades
- 10:00:03 → 25 trades
... (rest of the day)

Peak TPS = 50 tx/s
```

---

### 3. Avg Latency

**Logic:** Calculate the average end-to-end latency for all trades that reached the COMMITTED stage within the time window.

End-to-end latency = `timestamp_committed - timestamp_received`

Only trades that successfully completed the entire pipeline (reached COMMITTED) are included in this calculation.

**Example:**
```
Trade A: received at 10:00:00.000, committed at 10:00:02.500 → latency = 2500ms
Trade B: received at 10:00:01.000, committed at 10:00:03.200 → latency = 2200ms
Trade C: received at 10:00:02.000, committed at 10:00:04.800 → latency = 2800ms

Avg Latency = (2500 + 2200 + 2800) / 3 = 2500ms
```

---

### 4. DLQ Count (Dead Letter Queue Count)

**Logic:** Count the total number of error events sent to the DLQ (Dead Letter Queue) within the time window.

This represents trades or messages that failed processing and were moved to the DLQ for later investigation or manual intervention.

**Example:**
```
Last 24h:
- 3 trades failed validation and moved to DLQ
- 2 trades failed enrichment and moved to DLQ

DLQ Count = 5 errors
```

---

### 5. Invalid Trades

**Logic:** Count the total number of trades marked as invalid within the time window.

These are trades that failed business validation rules (e.g., invalid instrument, missing required fields, rule violations) and were logged as invalid.

**Example:**
```
Last 24h:
- 100 trades with missing counterparty
- 250 trades with invalid instrument code
- 35 trades with invalid quantity

Invalid Trades = 385 trades
```

---

## Trade Pipeline Depth (Stage-Level Metrics)

Each stage (RECEIVED, VALIDATED, ENRICHED, COMMITTED) tracks three metrics:

### Count

**Logic:** Number of trades currently at or that passed through this stage within the time window.

**Example:**
```
RECEIVED Count = 81677  (all trades that entered the system)
VALIDATED Count = 42377 (trades that passed validation)
ENRICHED Count = 1654   (trades that were enriched)
COMMITTED Count = 1654  (trades that completed the pipeline)
```

### Latency

**Logic:** Average time trades spend at this specific stage.

Stage latency = `timestamp_current_stage - timestamp_previous_stage`

- RECEIVED: No latency (entry point)
- VALIDATED: `timestamp_validated - timestamp_received`
- ENRICHED: `timestamp_enriched - timestamp_validated`
- COMMITTED: `timestamp_committed - timestamp_enriched`

**Example:**
```
Trade X:
- Received: 10:00:00.000
- Validated: 10:00:08.000 → VALIDATED latency = 8000ms
- Enriched: 10:00:48.000 → ENRICHED latency = 40000ms
- Committed: 10:01:00.000 → COMMITTED latency = 12000ms

If 100 trades have similar timings:
VALIDATED Latency = avg(8000ms) = 8000ms
ENRICHED Latency = avg(40000ms) = 40000ms
COMMITTED Latency = avg(12000ms) = 12000ms
```

### Success Rate

**Logic:** Percentage of trades from the previous stage that successfully completed the current stage.

Success Rate = (trades_at_current_stage / trades_at_previous_stage) × 100%

- RECEIVED: 100% (entry point, no previous stage)
- VALIDATED: (trades_validated / trades_received) × 100%
- ENRICHED: (trades_enriched / trades_validated) × 100%
- COMMITTED: (trades_committed / trades_enriched) × 100%

**Example:**
```
RECEIVED: 81677 trades
VALIDATED: 42377 trades → Success Rate = (42377 / 81677) × 100% = 51.9%
ENRICHED: 1654 trades → Success Rate = (1654 / 42377) × 100% = 3.9%
COMMITTED: 1654 trades → Success Rate = (1654 / 1654) × 100% = 100%

This shows that 51.9% of received trades passed validation,
and 3.9% of validated trades were enriched.
```

---

## Trend Charts

### TPS Trend

**Logic:** Time-series chart showing TPS (transactions per second) over time intervals.

Each data point represents the number of trades received in that specific time bucket (e.g., per second or per minute).

**Example:**
```
Time buckets (1-minute intervals):
T-2: 2000 trades
T-1: 10000 trades
T0: 8000 trades

Chart plots these points to show throughput trends over time.
```

---

### Latency Metrics (Avg, P95, P99)

**Logic:** Statistical distribution of latency values for trades at the COMMITTED stage.

- **Avg (Average):** Mean latency of all committed trades
- **P95 (95th Percentile):** 95% of trades completed within this latency
- **P99 (99th Percentile):** 99% of trades completed within this latency

**Example:**
```
100 committed trades with latencies (sorted):
[1000ms, 1100ms, ..., 12000ms, 13000ms, 15000ms, 20000ms]

Avg = sum of all / 100 = 13000ms
P95 = latency at position 95 = 15000ms (95% of trades completed within 15s)
P99 = latency at position 99 = 20000ms (99% of trades completed within 20s)
```

Higher percentiles (P95, P99) reveal tail latencies and help identify outliers or system bottlenecks.

---

## System Alerts

**Logic:** Rule-based threshold monitoring that generates alerts when metrics exceed configured limits.

Alert structure:
- **Severity:** CRITICAL, MEDIUM, LOW
- **Metric:** The measured value
- **Threshold:** The configured limit
- **Timestamp:** When the alert was triggered

**Example:**
```
Alert: "QUEUE_LAG : 81612.00 vs 50.00"
- Metric: Queue lag
- Measured value: 81612.00
- Threshold: 50.00
- Severity: CRITICAL (because measured >> threshold)
- Triggered: When queue lag exceeded 50

Alert: "LATENCY_P99 VALIDATED: 3046.00 vs 200.00"
- Metric: P99 latency at VALIDATED stage
- Measured value: 3046ms
- Threshold: 200ms
- Severity: CRITICAL
- Triggered: When 99th percentile latency exceeded 200ms

Alert: "TPS : 11.00 vs 10.00"
- Metric: Current TPS
- Measured value: 11 tx/s
- Threshold: 10 tx/s
- Severity: MEDIUM
- Triggered: When TPS exceeded configured limit
```

---

## Dead Letter Queue Details

### Total DLQ Count

**Logic:** Total count of all error events in the DLQ across all stages.

### Last Error

**Logic:** Timestamp of the most recent error event sent to DLQ.

**Example:**
```
If last error occurred at 10:35:42:
Last Error = "6 min ago" (relative to current time 10:41:42)
```

### Errors by Stage

**Logic:** Breakdown of DLQ errors grouped by the pipeline stage where the failure occurred.

**Example:**
```
RECEIVED: 10 errors (failed at entry validation)
VALIDATED: 50 errors (failed business validation)
ENRICHED: 25 errors (failed enrichment process)
COMMITTED: 5 errors (failed final commit)

Total DLQ Count = 90
``` 