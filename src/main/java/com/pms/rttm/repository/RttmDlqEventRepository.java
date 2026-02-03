package com.pms.rttm.repository;

import com.pms.rttm.entity.RttmDlqEventEntity;
import com.pms.rttm.enums.EventStage;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RttmDlqEventRepository
                extends JpaRepository<RttmDlqEventEntity, Long> {
        @Query("""
                            SELECT d.eventStage, COUNT(d)
                            FROM RttmDlqEventEntity d
                            GROUP BY d.eventStage
                        """)
        List<Object[]> countGroupedByStageRaw();

        default Map<EventStage, Long> countGroupedByStage() {
                return countGroupedByStageRaw().stream()
                                .collect(Collectors.toMap(
                                                r -> (EventStage) r[0],
                                                r -> (Long) r[1]));
        }

        long countByEventTimeAfter(Instant time);

        // Last 24 hours methods
        @Query("""
                            SELECT d.eventStage, COUNT(d)
                            FROM RttmDlqEventEntity d
                            WHERE d.eventTime >= :since
                            GROUP BY d.eventStage
                        """)
        List<Object[]> countGroupedByStageRawSince(@Param("since") Instant since);

        default Map<EventStage, Long> countGroupedByStageSince(Instant since) {
                return countGroupedByStageRawSince(since).stream()
                                .collect(Collectors.toMap(
                                                r -> (EventStage) r[0],
                                                r -> (Long) r[1]));
        }
}
