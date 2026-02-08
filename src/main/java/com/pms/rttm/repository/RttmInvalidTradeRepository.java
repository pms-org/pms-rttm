package com.pms.rttm.repository;

import com.pms.rttm.entity.RttmInvalidTradeEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface RttmInvalidTradeRepository extends JpaRepository<RttmInvalidTradeEntity, Long> {

    // Get total invalid trades count for last 24 hours
    @Query("""
                SELECT COUNT(i)
                FROM RttmInvalidTradeEntity i
                WHERE i.eventTime >= :since
            """)
    long countSince(@Param("since") Instant since);

    // Get total invalid trades count (all-time)
    long count();
}
