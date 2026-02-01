package com.pms.rttm.service;

import com.pms.rttm.repository.RttmQueueMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Scheduled cleanup service for queue metrics.
 * Deletes old metrics beyond the retention window to prevent database bloat.
 * Queue metrics arrive every 30s per service/topic/partition, leading to rapid
 * growth.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QueueMetricCleanupService {

    private final RttmQueueMetricRepository queueMetricRepository;

    @Value("${rttm.queue-metrics.retention-hours:48}")
    private int retentionHours;

    /**
     * Delete queue metrics older than retention window.
     * Default: Runs daily at 2 AM.
     * Configurable via: rttm.queue-metrics.cleanup-cron
     */
    @Scheduled(cron = "${rttm.queue-metrics.cleanup-cron:0 0 2 * * ?}")
    @Transactional
    public void cleanupOldMetrics() {
        Instant cutoff = Instant.now().minus(retentionHours, ChronoUnit.HOURS);

        log.info("Starting queue metrics cleanup (retention: {} hours, cutoff: {})", retentionHours, cutoff);

        try {
            int deleted = queueMetricRepository.deleteBySnapshotTimeBefore(cutoff);
            log.info("Successfully deleted {} queue metric records older than {}", deleted, cutoff);
        } catch (Exception ex) {
            log.error("Failed to cleanup old queue metrics", ex);
        }
    }

    /**
     * Get current retention count for monitoring
     */
    public long getCurrentMetricCount() {
        return queueMetricRepository.count();
    }
}
