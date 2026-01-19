package com.pms.rttm.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import com.pms.rttm.proto.RttmDlqEvent;

@Configuration
public class DlqEventConsumerConfig extends KafkaConsumerConfig {

    @Value("${rttm.consumer.concurrency:3}")
    private int concurrency;

    @Bean
    public ConsumerFactory<String, RttmDlqEvent> dlqConsumerFactory() {
        Map<String, Object> props = baseProps("rttm-dlq-consumer");
        props.put("specific.protobuf.value.type", RttmDlqEvent.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RttmDlqEvent> dlqListenerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, RttmDlqEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(dlqConsumerFactory());
        factory.setConcurrency(concurrency);
        factory.getContainerProperties().setAckMode(AckMode.MANUAL);
        factory.getContainerProperties().setPollTimeout(5000);
        return factory;
    }
}
