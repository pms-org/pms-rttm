package com.pms.rttm.repository;

import com.pms.rttm.entity.RttmQueueMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface RttmQueueMetricRepository
		extends JpaRepository<RttmQueueMetricEntity, Long> {

	/**
	 * Total current lag across all topics, partitions and consumer groups
	 * (latest snapshot per topic + partition + consumer group)
	 */
	@Query("""
			    SELECT COALESCE(SUM(q.producedOffset - q.consumedOffset), 0)
			    FROM RttmQueueMetricEntity q
			    WHERE q.snapshotTime = (
			        SELECT MAX(q2.snapshotTime)
			        FROM RttmQueueMetricEntity q2
			        WHERE q2.topicName = q.topicName
			          AND q2.partitionId = q.partitionId
			          AND q2.consumerGroup = q.consumerGroup
			    )
			""")
	long totalLag();

	/**
	 * Current lag per topic + partition (latest snapshot)
	 * Used for dashboards / heatmaps
	 */
	@Query("""
			    SELECT q.topicName,
			           q.partitionId,
			           (q.producedOffset - q.consumedOffset)
			    FROM RttmQueueMetricEntity q
			    WHERE q.snapshotTime = (
			        SELECT MAX(q2.snapshotTime)
			        FROM RttmQueueMetricEntity q2
			        WHERE q2.topicName = q.topicName
			          AND q2.partitionId = q.partitionId
			          AND q2.consumerGroup = q.consumerGroup
			    )
			""")
	List<Object[]> lagByTopicPartitionRaw();

	/**
	 * Current lag per partition (aggregated across topics if needed)
	 * Latest snapshot only
	 */
	@Query("""
			    SELECT q.partitionId,
			           (q.producedOffset - q.consumedOffset)
			    FROM RttmQueueMetricEntity q
			    WHERE q.snapshotTime = (
			        SELECT MAX(q2.snapshotTime)
			        FROM RttmQueueMetricEntity q2
			        WHERE q2.topicName = q.topicName
			          AND q2.partitionId = q.partitionId
			          AND q2.consumerGroup = q.consumerGroup
			    )
			""")
	List<Object[]> lagByPartitionRaw();

	/**
	 * Convenience mapper for partition → lag
	 */
	default Map<Integer, Long> lagByPartition() {
		return lagByPartitionRaw()
				.stream()
				.collect(Collectors.toMap(
						r -> ((Number) r[0]).intValue(),
						r -> ((Number) r[1]).longValue(),
						(oldV, newV) -> oldV + newV));
	}
}
