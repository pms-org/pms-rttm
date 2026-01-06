package com.pms.rttm.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import com.pms.rttm.proto.RttmErrorEvent;

@Configuration
public class ErrorEventConsumerConfig extends KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, RttmErrorEvent> errorConsumerFactory() {
        Map<String, Object> props = baseProps("rttm-error-consumer");
        props.put("specific.protobuf.value.type", RttmErrorEvent.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RttmErrorEvent> errorListenerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, RttmErrorEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(errorConsumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(AckMode.MANUAL);
        factory.getContainerProperties().setPollTimeout(5000);
        return factory;
    }
}
