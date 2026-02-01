package com.pms.rttm.tools;

import com.pms.rttm.proto.RttmTradeEvent;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Future;

/**
 * High-volume producer to trigger alert conditions:
 * - High TPS (triggers TPS alerts)
 * - High latency trades (triggers latency alerts)
 * - Fast burst sends to test queue depth alerts
 * 
 * Usage:
 * set KAFKA_BOOTSTRAP=localhost:9092
 * set SCHEMA_REGISTRY_URL=http://localhost:8081
 * mvn compile exec:java -Dexec.mainClass=com.pms.rttm.tools.AlertTestProducer
 * -Dexec.args="100"
 */
public class AlertTestProducer {

    private static final String[] PORTFOLIO_IDS = {
            "b23d70cf-6d7a-48b5-8e8c-4eda8f4d611d",
            "7839ee86-6527-43ed-94d6-53dadda2bd9a",
            "7d08a041-6aed-421c-b895-1d94d4401b79",
            "7ecae4ee-a6df-4237-8e9a-5e4a5e417fa3",
            "8deeeb23-8862-4e29-8626-e538dddd868f"
    };

    private static final String[] STOCKS = {
            "AAPL", "MSFT", "GOOGL", "AMZN", "META", "NVDA", "NFLX", "AMD"
    };

    private static final String[][] STAGES = {
            { "RECEIVED", "TRADE_RECEIVED", "Trade received", "pms-trade-capture", "cg-ingest", "trades.raw" },
            { "VALIDATED", "TRADE_VALIDATED", "Trade validated", "pms-validation", "cg-validate", "trades.validated" },
            { "ENRICHED", "TRADE_ENRICHED", "Trade enriched", "pms-transactional", "cg-enrich", "trades.enriched" },
            { "COMMITTED", "TRADE_COMMITTED", "Trade committed", "pms-transactional", "cg-commit", "trades.committed" },
            { "ANALYZED", "TRADE_ANALYZED", "Trade analyzed", "pms-analytics", "cg-analyze", "trades.analyzed" }
    };

    public static void main(String[] args) throws Exception {
        String bootstrap = System.getenv().getOrDefault("KAFKA_BOOTSTRAP", "localhost:9092");
        String schemaRegistry = System.getenv().getOrDefault("SCHEMA_REGISTRY_URL", "http://localhost:8081");
        String topic = "rttm.trade.events";
        int burstSize = 100;

        if (args.length > 0) {
            burstSize = Integer.parseInt(args[0]);
        }

        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrap);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", KafkaProtobufSerializer.class.getName());
        props.put("schema.registry.url", schemaRegistry);

        Random random = new Random();

        try (KafkaProducer<String, RttmTradeEvent> producer = new KafkaProducer<>(props)) {
            System.out.printf("[ALERT TEST] Sending %d trades in RAPID BURST to trigger alerts...%n",
                    burstSize);
            System.out.println("Expected alerts:");
            System.out.println("  - TPS alert (current threshold: 10 TPS)");
            System.out.println("  - Latency alert (current threshold: 50ms warning / 200ms critical)");
            System.out.println("  - Queue depth alert (if consuming can't keep up)");
            System.out.println();

            long startTime = System.currentTimeMillis();
            int totalEvents = 0;

            for (int tradeNum = 0; tradeNum < burstSize; tradeNum++) {
                String tradeId = UUID.randomUUID().toString();
                String portfolioId = PORTFOLIO_IDS[random.nextInt(PORTFOLIO_IDS.length)];
                String stock = STOCKS[random.nextInt(STOCKS.length)];
                long baseTime = Instant.now().toEpochMilli();

                for (int stageIdx = 0; stageIdx < STAGES.length; stageIdx++) {
                    String[] stage = STAGES[stageIdx];
                    String eventStage = stage[0];
                    String eventType = stage[1];
                    String message = stage[2] + " for " + stock;
                    String serviceName = stage[3];
                    String consumerGroup = stage[4];
                    String topicName = stage[5];

                    // HIGH LATENCY: 100-400ms between stages to trigger latency alerts
                    long stageLatency = 100 + random.nextInt(300);
                    long eventTime = baseTime + (stageIdx * stageLatency);

                    RttmTradeEvent msg = RttmTradeEvent.newBuilder()
                            .setTradeId(tradeId)
                            .setServiceName(serviceName)
                            .setEventType(eventType)
                            .setEventStage(eventStage)
                            .setEventStatus("SUCCESS")
                            .setSourceQueue("queue.in")
                            .setTargetQueue("queue.out")
                            .setTopicName(topicName)
                            .setConsumerGroup(consumerGroup)
                            .setPartitionId(random.nextInt(3))
                            .setOffsetValue(10000 + tradeNum * 10 + stageIdx)
                            .setEventTime(eventTime)
                            .setMessage(message)
                            .build();

                    ProducerRecord<String, RttmTradeEvent> record = new ProducerRecord<>(topic, tradeId, msg);
                    producer.send(record);
                    totalEvents++;

                    // NO DELAY - send as fast as possible to trigger TPS alerts
                }

                if ((tradeNum + 1) % 10 == 0) {
                    System.out.printf("  Sent %d trades (%d events)...%n", tradeNum + 1, totalEvents);
                }
            }

            producer.flush();
            long endTime = System.currentTimeMillis();
            long durationMs = endTime - startTime;
            double tps = (totalEvents * 1000.0) / durationMs;

            System.out.printf("%n[COMPLETE] Burst complete:%n");
            System.out.printf("   - Sent: %d trades × 5 stages = %d total events%n", burstSize, totalEvents);
            System.out.printf("   - Duration: %d ms%n", durationMs);
            System.out.printf("   - Throughput: %.1f TPS%n", tps);
            System.out.printf("%n[INFO] Check RTTM logs for alerts in ~60 seconds!%n");
        }
    }
}
