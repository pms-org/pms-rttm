| RTTM Metric | Tables Used                              |
| ----------- | ---------------------------------------- |
| TPS         | `rttm_trade_events`                      |
| Queue Lag   | `rttm_queue_metrics`                     |
| DLQ Depth   | `rttm_dlq_events`                        |
| Error Rate  | `rttm_error_events`, `rttm_trade_events` |
| Alerts      | `rttm_alert_thresholds`, `rttm_alerts`   |

RTTM KAFKA TOPIC DESIGN

We do NOT create one topic per metric.
We create semantic topics, aligned with the data tables you already designed.
| Topic Name           | Purpose                    |
| -------------------- | -------------------------- |
| `rttm.trade.events`  | Trade lifecycle & movement |
| `rttm.queue.metrics` | Queue lag snapshots        |
| `rttm.dlq.events`    | DLQ routing                |
| `rttm.error.events`  | Errors & failures          |
| `rttm.log.events`    | Parsed/interpreted logs    |
