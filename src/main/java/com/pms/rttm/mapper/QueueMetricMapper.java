package com.pms.rttm.mapper;

import com.pms.rttm.entity.RttmQueueMetricEntity;
import com.pms.rttm.proto.RttmQueueMetric;

public final class QueueMetricMapper {

    private QueueMetricMapper() {
    }

    public static RttmQueueMetricEntity toEntity(RttmQueueMetric proto) {

        return RttmQueueMetricEntity.builder()
                .serviceName(proto.getServiceName())
                .topicName(proto.getTopic())
                .partitionId(proto.getPartition())
                .producedOffset(proto.getProducedOffset())
                .consumedOffset(proto.getConsumedOffset())
                .consumerGroup(proto.getConsumerGroup())
                .snapshotTime(proto.getSnapshotTime())
                .build();
    }
}
