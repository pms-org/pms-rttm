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
 * Enhanced producer that sends complete trade lifecycle events (RECEIVED →
 * VALIDATED → ENRICHED → COMMITTED → ANALYZED)
 * for testing stage latency computation and alert generation.
 * 
 * Usage:
 * set KAFKA_BOOTSTRAP=localhost:9092
 * set SCHEMA_REGISTRY_URL=http://localhost:8081
 * mvn compile exec:java -Dexec.mainClass=com.pms.rttm.tools.TradeProducer
 * -Dexec.args="5"
 */
public class TradeProducer {

        // Valid portfolio IDs from validation test data
        private static final String[] PORTFOLIO_IDS = {
                        "b23d70cf-6d7a-48b5-8e8c-4eda8f4d611d",
                        "7839ee86-6527-43ed-94d6-53dadda2bd9a",
                        "7d08a041-6aed-421c-b895-1d94d4401b79",
                        "7ecae4ee-a6df-4237-8e9a-5e4a5e417fa3",
                        "8deeeb23-8862-4e29-8626-e538dddd868f",
                        "a1d62557-221d-4799-bfba-4d7215dcdac3",
                        "b94926b8-4919-4279-8470-1f3ec5b1b0fc",
                        "ca7377ab-2596-47a3-9c3d-4089206fa3de",
                        "d3a485c8-3e2f-4fa3-a823-254748942200",
                        "e9fc6225-6845-4c91-815f-ba3e9ea06e21"
        };

        // Valid stock symbols from validation test data
        private static final String[] STOCKS = {
                        "AAPL", "MSFT", "GOOGL", "AMZN", "META", "NVDA", "NFLX",
                        "AMD", "INTC", "IBM", "ORCL", "BAC", "JPM", "WMT"
        };

        // Pipeline stages in order mapped to actual services
        private static final String[][] STAGES = {
                        { "RECEIVED", "TRADE_RECEIVED", "Trade received", "pms-trade-capture", "cg-ingest", "trade.in",
                                        "trade.validate", "trades.raw" },
                        { "VALIDATED", "TRADE_VALIDATED", "Trade validated", "pms-validation", "cg-validate",
                                        "trade.validate", "trade.enrich", "trades.validated" },
                        { "ENRICHED", "TRADE_ENRICHED", "Trade enriched", "pms-transactional", "cg-enrich",
                                        "trade.enrich",
                                        "trade.commit", "trades.enriched" },
                        { "COMMITTED", "TRADE_COMMITTED", "Trade committed", "pms-transactional", "cg-commit",
                                        "trade.commit", "trade.analyze", "trades.committed" },
                        { "ANALYZED", "TRADE_ANALYZED", "Trade analyzed", "pms-analytics", "cg-analyze",
                                        "trade.analyze", "trade.complete", "trades.analyzed" }
        };

        public static void main(String[] args) throws Exception {
                String bootstrap = System.getenv().getOrDefault("KAFKA_BOOTSTRAP", "localhost:9092");
                String schemaRegistry = System.getenv().getOrDefault("SCHEMA_REGISTRY_URL", "http://localhost:8081");
                String topic = "rttm.trade.events";
                int tradeCount = 5;

                if (args.length > 0) {
                        tradeCount = Integer.parseInt(args[0]);
                }

                Properties props = new Properties();
                props.put("bootstrap.servers", bootstrap);
                props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
                props.put("value.serializer", KafkaProtobufSerializer.class.getName());
                props.put("schema.registry.url", schemaRegistry);

                Random random = new Random();

                try (KafkaProducer<String, RttmTradeEvent> producer = new KafkaProducer<>(props)) {
                        System.out.printf("Sending %d complete trade lifecycles (each with 5 stages)...%n", tradeCount);

                        for (int tradeNum = 0; tradeNum < tradeCount; tradeNum++) {
                                String tradeId = UUID.randomUUID().toString();
                                String portfolioId = PORTFOLIO_IDS[random.nextInt(PORTFOLIO_IDS.length)];
                                String stock = STOCKS[random.nextInt(STOCKS.length)];
                                long baseTime = Instant.now().toEpochMilli();

                                System.out.printf("%nTrade #%d: %s (Portfolio: %s, Stock: %s)%n",
                                                tradeNum + 1, tradeId.substring(0, 8), portfolioId.substring(0, 8),
                                                stock);

                                for (int stageIdx = 0; stageIdx < STAGES.length; stageIdx++) {
                                        String[] stage = STAGES[stageIdx];
                                        String eventStage = stage[0];
                                        String eventType = stage[1];
                                        String message = stage[2] + " for " + stock;
                                        String serviceName = stage[3];
                                        String consumerGroup = stage[4];
                                        String sourceQueue = stage[5];
                                        String targetQueue = stage[6];
                                        String topicName = stage[7];

                                        // Latency between stages: 50-300ms to trigger alerts
                                        long stageLatency = 50 + random.nextInt(250);
                                        long eventTime = baseTime + (stageIdx * stageLatency);

                                        RttmTradeEvent msg = RttmTradeEvent.newBuilder()
                                                        .setTradeId(tradeId)
                                                        .setServiceName(serviceName)
                                                        .setEventType(eventType)
                                                        .setEventStage(eventStage)
                                                        .setEventStatus("SUCCESS")
                                                        .setSourceQueue(sourceQueue)
                                                        .setTargetQueue(targetQueue)
                                                        .setTopicName(topicName)
                                                        .setConsumerGroup(consumerGroup)
                                                        .setPartitionId(random.nextInt(3))
                                                        .setOffsetValue(1000 + tradeNum * 10 + stageIdx)
                                                        .setEventTime(eventTime)
                                                        .setMessage(message)
                                                        .build();

                                        ProducerRecord<String, RttmTradeEvent> record = new ProducerRecord<>(topic,
                                                        tradeId, msg);
                                        Future<RecordMetadata> f = producer.send(record);
                                        RecordMetadata meta = f.get();

                                        System.out.printf("  [OK] %s (latency: %dms) => offset=%d%n",
                                                        eventStage, stageLatency, meta.offset());

                                        Thread.sleep(10);
                                }
                        }
                        producer.flush();
                        System.out.printf("%n[DONE] Sent %d trades x 5 stages = %d total events%n",
                                        tradeCount, tradeCount * 5);
                }
        }
}