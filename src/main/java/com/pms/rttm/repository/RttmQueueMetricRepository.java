package com.pms.rttm.repository;

import com.pms.rttm.entity.RttmQueueMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public interface RttmQueueMetricRepository
		extends JpaRepository<RttmQueueMetricEntity, Long> {

	// Total current lag across all topics, partitions and consumer groups (latest
	// snapshot per topic + partition + consumer group)
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

	// Current lag per topic + partition (latest snapshot), Used for dashboards /
	// heatmaps
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

	// Current lag per partition (aggregated across topics if needed) Latest
	// snapshot only
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

	// Convenience mapper for partition → lag
	default Map<Integer, Long> lagByPartition() {
		return lagByPartitionRaw()
				.stream()
				.collect(Collectors.toMap(
						r -> ((Number) r[0]).intValue(),
						r -> ((Number) r[1]).longValue(),
						(oldV, newV) -> oldV + newV));
	}

	// ================= LAST 24 HOURS AVERAGE LAG =================

	// Average total lag across all partitions for last 24 hours
	@Query("""
				SELECT COALESCE(AVG(q.producedOffset - q.consumedOffset), 0)
				FROM RttmQueueMetricEntity q
				WHERE q.snapshotTime >= :since
			""")
	long avgTotalLagSince(@Param("since") Instant since);

	// Average lag per partition for last 24 hours
	@Query("""
				SELECT q.partitionId,
					   AVG(q.producedOffset - q.consumedOffset)
				FROM RttmQueueMetricEntity q
				WHERE q.snapshotTime >= :since
				GROUP BY q.partitionId
			""")
	List<Object[]> avgLagByPartitionRawSince(@Param("since") Instant since);

	default Map<Integer, Long> avgLagByPartitionSince(Instant since) {
		return avgLagByPartitionRawSince(since)
				.stream()
				.collect(Collectors.toMap(
						r -> ((Number) r[0]).intValue(),
						r -> ((Number) r[1]).longValue()));
	}

	// Max queue depth since a given time (for alert generation)
	@Query("""
				SELECT COALESCE(MAX(q.producedOffset - q.consumedOffset), 0)
				FROM RttmQueueMetricEntity q
				WHERE q.snapshotTime >= :since
			""")
	Long findMaxQueueDepthSince(@Param("since") Instant since);

	/*
	 * Delete old queue metrics beyond retention window.
	 * Used by cleanup service to prevent database bloat.
	 */
	@Modifying
	@Query("DELETE FROM RttmQueueMetricEntity q WHERE q.snapshotTime < :cutoff")
	int deleteBySnapshotTimeBefore(@Param("cutoff") Instant cutoff);
}
