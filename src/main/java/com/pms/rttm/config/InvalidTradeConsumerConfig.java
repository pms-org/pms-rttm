package com.pms.rttm.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import com.pms.validation.proto.InvalidTradeEventProto;

@Configuration
public class InvalidTradeConsumerConfig extends KafkaConsumerConfig {

    @Value("${rttm.consumer.concurrency:3}")
    private int concurrency;

    @Bean
    public ConsumerFactory<String, InvalidTradeEventProto> invalidTradeConsumerFactory() {
        Map<String, Object> props = baseProps("rttm-invalid-trade-consumer");
        props.put("specific.protobuf.value.type", InvalidTradeEventProto.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InvalidTradeEventProto> invalidTradeListenerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, InvalidTradeEventProto> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(invalidTradeConsumerFactory());
        factory.setConcurrency(concurrency);
        factory.getContainerProperties().setAckMode(AckMode.MANUAL);
        factory.getContainerProperties().setPollTimeout(5000);
        return factory;
    }
}
