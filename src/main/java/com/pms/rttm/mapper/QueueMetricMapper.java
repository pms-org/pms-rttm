package com.pms.rttm.mapper;

import java.time.Instant;

import com.pms.rttm.entity.RttmQueueMetricEntity;
import com.pms.rttm.proto.RttmQueueMetric;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class QueueMetricMapper {

    public static RttmQueueMetricEntity toEntity(RttmQueueMetric proto) {

        return RttmQueueMetricEntity.builder()
                .serviceName(proto.getServiceName())
                .topicName(proto.getTopicName())
                .partitionId(proto.getPartitionId())
                .producedOffset(proto.getProducedOffset())
                .consumedOffset(proto.getConsumedOffset())
                .consumerGroup(proto.getConsumerGroup())
                .snapshotTime(Instant.ofEpochMilli(proto.getSnapshotTime()))
                .build();
    }
}
