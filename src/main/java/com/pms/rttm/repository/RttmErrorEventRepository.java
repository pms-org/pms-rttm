package com.pms.rttm.repository;

import com.pms.rttm.entity.RttmErrorEventEntity;
import com.pms.rttm.enums.EventStage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface RttmErrorEventRepository
                extends JpaRepository<RttmErrorEventEntity, Long> {
        @Query("""
                            SELECT COUNT(e)
                            FROM RttmErrorEventEntity e
                            WHERE e.eventStage = :stage
                        """)
        long countByStage(@Param("stage") EventStage stage);

        @Query("""
                            SELECT COUNT(e)
                            FROM RttmErrorEventEntity e
                            WHERE e.eventStage = :stage
                              AND e.eventTime >= :since
                        """)
        long countByStageSince(@Param("stage") EventStage stage, @Param("since") Instant since);

        long countByEventTimeAfter(Instant time);
}
