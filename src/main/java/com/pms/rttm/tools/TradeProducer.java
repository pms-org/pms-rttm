package com.pms.rttm.tools;

import com.pms.rttm.proto.RttmTradeEvent;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.time.Instant;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Simple producer that sends N RttmTradeEvent protobuf messages to topic
 * rttm.trade.events.
 * set KAFKA_BOOTSTRAP=localhost:9092
 * set SCHEMA_REGISTRY_URL=http://localhost:8081
 * mvn compile exec:java -Dexec.mainClass=com.pms.rttm.tools.TradeProducer
 */
public class TradeProducer {

    public static void main(String[] args) throws Exception {
        String bootstrap = System.getenv().getOrDefault("KAFKA_BOOTSTRAP", "localhost:9092");
        String schemaRegistry = System.getenv().getOrDefault("SCHEMA_REGISTRY_URL", "http://localhost:8081");
        String topic = "rttm.trade.events";
        int count = 10;
        if (args.length > 0) {
            count = Integer.parseInt(args[0]);
        }

        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrap);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        // Use Confluent Protobuf serializer for the value
        props.put("value.serializer", KafkaProtobufSerializer.class.getName());
        props.put("schema.registry.url", schemaRegistry);

        // Optional: register as latest or use subject naming strategy if needed.
        // props.put("auto.register.schemas", true);

        // Hardcoded dataset rows (based on rttm_trade_events.csv). Each entry:
        // {tradeId, consumerGroup, eventStage, eventStatus, eventType, message, offset,
        // partition, serviceName, sourceQueue, targetQueue, topicName}
        String[][] rows = new String[][] {
                { "a2c04c8f-aa0b-4f6d-bd56-f103f612fcc1", "cg-ingest", "RECEIVED", "SUCCESS", "TRADE_RECEIVED",
                        "Trade received", "1000", "0", "trade-ingest", "trade.in", "trade.validate", "trades.raw" },
                { "2437b8ca-5d13-428d-b6ea-85fa5647d86e", "cg-validate", "VALIDATED", "SUCCESS", "TRADE_VALIDATED",
                        "Trade validated", "2000", "1", "trade-validator", "trade.validate", "trade.enrich",
                        "trades.validated" },
                { "a5ffcea6-31c6-4c10-977d-aa4965d82902", "cg-ingest", "RECEIVED", "SUCCESS", "TRADE_RECEIVED",
                        "Trade received", "1002", "0", "trade-ingest", "trade.in", "trade.validate", "trades.raw" },
                { "4a4b1542-d438-4d7d-86d3-8119f25566d2", "cg-ingest", "RECEIVED", "SUCCESS", "TRADE_RECEIVED",
                        "Trade received", "1003", "0", "trade-ingest", "trade.in", "trade.validate", "trades.raw" },
                { "2b871eed-c638-436b-8d17-8f98bd64b1ec", "cg-ingest", "RECEIVED", "SUCCESS", "TRADE_RECEIVED",
                        "Trade received", "1004", "0", "trade-ingest", "trade.in", "trade.validate", "trades.raw" },
                { "2e68899f-7b7c-4284-8195-4b2531491d94", "cg-ingest", "RECEIVED", "SUCCESS", "TRADE_RECEIVED",
                        "Trade received", "1005", "0", "trade-ingest", "trade.in", "trade.validate", "trades.raw" },
                { "9054be3a-2912-4360-808f-6421a44500a6", "cg-ingest", "RECEIVED", "SUCCESS", "TRADE_RECEIVED",
                        "Trade received", "1006", "0", "trade-ingest", "trade.in", "trade.validate", "trades.raw" },
                { "4970a661-8d82-449a-8aa2-4a39f18786ae", "cg-ingest", "RECEIVED", "SUCCESS", "TRADE_RECEIVED",
                        "Trade received", "1007", "0", "trade-ingest", "trade.in", "trade.validate", "trades.raw" },
                { "3722ed60-07a8-4f64-b9c0-faf2d00ef194", "cg-ingest", "RECEIVED", "SUCCESS", "TRADE_RECEIVED",
                        "Trade received", "1008", "0", "trade-ingest", "trade.in", "trade.validate", "trades.raw" },
                { "e75447ad-0a05-4c79-b54c-25d5d94d115b", "cg-ingest", "RECEIVED", "SUCCESS", "TRADE_RECEIVED",
                        "Trade received", "1009", "0", "trade-ingest", "trade.in", "trade.validate", "trades.raw" }
        };

        try (KafkaProducer<String, RttmTradeEvent> producer = new KafkaProducer<>(props)) {
            int toSend = Math.min(count, rows.length);
            long now = Instant.now().toEpochMilli();
            for (int i = 0; i < toSend; i++) {
                String[] r = rows[i];
                String tradeId = r[0];
                String consumerGroup = r[1];
                String eventStage = r[2];
                String eventStatus = r[3];
                String eventType = r[4];
                String message = r[5];
                long offsetValue = Long.parseLong(r[6]);
                int partitionId = Integer.parseInt(r[7]);
                String serviceName = r[8];
                String sourceQueue = r[9];
                String targetQueue = r[10];
                String topicName = r[11];

                RttmTradeEvent msg = RttmTradeEvent.newBuilder()
                        .setTradeId(UUID.randomUUID().toString())
                        .setServiceName(serviceName)
                        .setEventType(eventType)
                        .setEventStage(eventStage)
                        .setEventStatus(eventStatus)
                        .setSourceQueue(sourceQueue)
                        .setTargetQueue(targetQueue)
                        .setTopicName(topicName)
                        .setConsumerGroup(consumerGroup)
                        .setPartitionId(partitionId)
                        .setOffsetValue(offsetValue)
                        .setEventTime(now + i * 1000)
                        .setMessage(message + " " + (i + 1))
                        .build();

                ProducerRecord<String, RttmTradeEvent> record = new ProducerRecord<>(topic, tradeId, msg);
                Future<RecordMetadata> f = producer.send(record);
                RecordMetadata meta = f.get(); // wait for send
                System.out.printf("Sent msg#%d => topic=%s partition=%d offset=%d%n",
                        i + 1, meta.topic(), meta.partition(), meta.offset());
            }
            producer.flush();
        }

        System.out.println("Done sending messages.");
    }
}