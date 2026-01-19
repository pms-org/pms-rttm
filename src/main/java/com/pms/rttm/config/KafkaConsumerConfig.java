package com.pms.rttm.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${schema.registry.url}")
    private String schemaRegistryUrl;
    @Value("${rttm.batch.size:100}")
    private int rttmBatchSize;
    @Value("${rttm.fetch.max-wait-ms:3000}")
    private int rttmFetchMaxWaitMs;

    protected Map<String, Object> baseProps(String groupId) {

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class);

        props.put("schema.registry.url", schemaRegistryUrl);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, rttmBatchSize);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, rttmFetchMaxWaitMs);

        return props;
    }

    // @Bean
    // public ConcurrentKafkaListenerContainerFactory<String,
    // RttmEventProto.RttmEvent> tradeEventKafkaListenerFactory() {

    // ConcurrentKafkaListenerContainerFactory<String, RttmEventProto.RttmEvent>
    // factory = new ConcurrentKafkaListenerContainerFactory<>();

    // factory.setConsumerFactory(tradeEventConsumerFactory());
    // factory.setConcurrency(3);
    // factory.getContainerProperties().setAckMode(AckMode.MANUAL);
    // factory.getContainerProperties().setPollTimeout(5000);
    // return factory;
    // }

}
