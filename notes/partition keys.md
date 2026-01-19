| Topic                    | Partition Key                   |
| ------------------------ | ------------------------------- |
| `rttm.trade.events`      | `trade_id`                      |
| `rttm.queue.metrics`     | `service_name + queue_name`     |
| `rttm.error.events`      | `service_name + error_code`     |
| `rttm.dlq.events`        | `original_topic + service_name` |

---
| Topic                    | Partition Key                   |
| ------------------------ | ------------------------------- |
| `rttm.service.heartbeat` | `service_name`                  |
| `rttm.alert.events`      | `alert_type + severity`         |
