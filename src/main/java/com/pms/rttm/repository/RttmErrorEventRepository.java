package com.pms.rttm.repository;

import com.pms.rttm.entity.RttmErrorEventEntity;
import com.pms.rttm.enums.EventStage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RttmErrorEventRepository
                extends JpaRepository<RttmErrorEventEntity, Long> {
        @Query("""
                            SELECT COUNT(e)
                            FROM RttmErrorEventEntity e
                            WHERE e.eventStage = :stage
                        """)
        long countByStage(@Param("stage") EventStage stage);
}
