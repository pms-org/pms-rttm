package com.pms.rttm.repository;

import com.pms.rttm.entity.RttmTradeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RttmTradeEventRepository extends JpaRepository<RttmTradeEvent, Long> {
    
    List<RttmTradeEvent> findByTradeIdOrderByEventTimeDesc(String tradeId);
    
    List<RttmTradeEvent> findByServiceNameAndEventTimeBetween(
        String serviceName, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COUNT(r) FROM RttmTradeEvent r WHERE r.eventStatus = 'FAILED' AND r.eventTime >= :since")
    Long countFailedEventsSince(@Param("since") LocalDateTime since);
}