package com.pms.rttm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaMetricsService {

    private final KafkaAdmin kafkaAdmin;
    private final JdbcTemplate jdbcTemplate;

    @Scheduled(fixedRate = 10000)
    public void collectPartitionMetrics() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            
            // Get topic partitions
            DescribeTopicsResult topicsResult = adminClient.describeTopics(Collections.singletonList("lifecycle.event"));
            var topicDescription = topicsResult.values().get("lifecycle.event").get();
            
            for (var partition : topicDescription.partitions()) {
                int partitionId = partition.partition();
                
                // Get latest offset (produced)
                var topicPartition = new TopicPartition("lifecycle.event", partitionId);
                var endOffsets = adminClient.listOffsets(Map.of(topicPartition, 
                    org.apache.kafka.clients.admin.OffsetSpec.latest())).all().get();
                long producedOffset = endOffsets.get(topicPartition).offset();
                
                // Get consumer group offset (consumed)
                var consumerGroupOffsets = adminClient.listConsumerGroupOffsets("pms-rttm-group").partitionsToOffsetAndMetadata().get();
                OffsetAndMetadata offsetMetadata = consumerGroupOffsets.get(topicPartition);
                long consumedOffset = offsetMetadata != null ? offsetMetadata.offset() : 0;
                
                // Store metrics
                String sql = """
                    INSERT INTO rttm_queue_metrics 
                    (service_name, topic_name, partition_id, produced_offset, consumed_offset, consumer_group, snapshot_time)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;
                
                jdbcTemplate.update(sql, "rttm-service", "lifecycle.event", partitionId, 
                                  producedOffset, consumedOffset, "pms-rttm-group", LocalDateTime.now());
            }
            
        } catch (Exception e) {
            log.error("Failed to collect Kafka metrics", e);
        }
    }
}