package com.pms.rttm.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;

import com.pms.rttm.proto.RttmTradeEvent;

@Configuration
public class TradeEventConsumerConfig extends KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, RttmTradeEvent> tradeEventConsumerFactory() {
        Map<String, Object> props = baseProps("rttm-trade-consumer");
        props.put("specific.protobuf.value.type", RttmTradeEvent.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    public ConcurrentKafkaListenerContainerFactory<String, RttmTradeEvent> tradeEventKafkaListenerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, RttmTradeEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(tradeEventConsumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(AckMode.MANUAL);
        factory.getContainerProperties().setPollTimeout(5000);
        return factory;
    }
}
