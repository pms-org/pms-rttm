package com.pms.rttm.repository;

import com.pms.rttm.entity.RttmQueueMetricEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RttmQueueMetricRepository
                extends JpaRepository<RttmQueueMetricEntity, Long> {
        @Query("""
                            SELECT COALESCE(SUM(q.lag), 0)
                            FROM RttmQueueMetricEntity q
                        """)
        long totalLag();

        @Query("""
                            SELECT q.partition, q.lag
                            FROM RttmQueueMetricEntity q
                        """)
        List<Object[]> lagByPartitionRaw();

        default Map<Integer, Long> lagByPartition() {
                return lagByPartitionRaw().stream()
                                .collect(Collectors.toMap(
                                                r -> (Integer) r[0],
                                                r -> (Long) r[1]));
        }
}
