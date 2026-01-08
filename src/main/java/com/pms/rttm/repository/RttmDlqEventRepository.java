package com.pms.rttm.repository;

import com.pms.rttm.entity.RttmDlqEventEntity;
import com.pms.rttm.enums.EventStage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
