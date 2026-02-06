package com.pms.rttm.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pms.rttm.entity.RttmQueueMetricEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * Redis Sentinel configuration for queue metrics aggregation cache.
 * Shared across all K8s pods for consistent aggregation decisions.
 * Provides high availability with automatic failover.
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.sentinel.master}")
    private String sentinelMaster;

    @Value("${spring.data.redis.sentinel.nodes}")
    private String sentinelNodes;

    @Value("${spring.data.redis.timeout}")
    private Duration redisTimeout;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.sentinel.password:}")
    private String sentinelPassword;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();
        sentinelConfig.master(sentinelMaster);

        // Parse sentinel nodes
        String[] nodes = sentinelNodes.split(",");
        for (String node : nodes) {
            String[] parts = node.trim().split(":");
            if (parts.length == 2) {
                sentinelConfig.sentinel(new RedisNode(parts[0], Integer.parseInt(parts[1])));
            }
        }

        // Set passwords if provided
        if (StringUtils.hasText(redisPassword)) {
            sentinelConfig.setPassword(redisPassword);
        }

        if (StringUtils.hasText(sentinelPassword)) {
            sentinelConfig.setSentinelPassword(sentinelPassword);
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(sentinelConfig);
        factory.setTimeout(redisTimeout.toMillis());
        return factory;
    }

    @Bean
    public RedisTemplate<String, RttmQueueMetricEntity> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, RttmQueueMetricEntity> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Configure ObjectMapper with JavaTimeModule for Instant serialization
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Enable default typing to preserve type information during serialization
        // This prevents ClassCastException when deserializing from Redis
        BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(RttmQueueMetricEntity.class)
                .build();
        objectMapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        // Use JSON serializer for values with configured ObjectMapper
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
