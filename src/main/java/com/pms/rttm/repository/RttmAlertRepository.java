package com.pms.rttm.repository;

import com.pms.rttm.entity.RttmAlertEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RttmAlertRepository extends JpaRepository<RttmAlertEntity, Long> {

    List<RttmAlertEntity> findByStatusOrderByTriggeredTimeDesc(String status, Pageable pageable);

}
