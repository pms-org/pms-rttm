package com.pms.rttm.service;

import com.pms.rttm.entity.RttmQueueMetricEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/*
 * Redis-based aggregation service to reduce queue metric storage in K8s environments.
 * Shared cache across all pods ensures consistent aggregation decisions.
 * Only stores metrics when:
 * 1. Lag changes significantly (>10% by default)
 * 2. Minimum time interval elapsed (5 minutes by default)
 * Uses Redis with automatic TTL expiry (24h default) to prevent memory leaks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QueueMetricAggregationService {

    private static final String CACHE_KEY_PREFIX = "rttm:queue_metric_cache:";

    private final RedisTemplate<String, RttmQueueMetricEntity> redisTemplate;

    @Value("${rttm.queue-metrics.aggregation-threshold:0.1}")
    private double lagChangeThreshold;

    @Value("${rttm.queue-metrics.min-interval-minutes:5}")
    private int minIntervalMinutes;

    @Value("${rttm.queue-metrics.cache-ttl-hours:24}")
    private int cacheTtlHours;

    // Determine if metric should be stored based on aggregation rules
    // Uses Redis to share cache across K8s pods
    public boolean shouldStore(RttmQueueMetricEntity newMetric) {
        String cacheKey = buildCacheKey(newMetric);

        try {
            RttmQueueMetricEntity previous = redisTemplate.opsForValue().get(cacheKey);

            // Always store first metric for this key
            if (previous == null) {
                redisTemplate.opsForValue().set(cacheKey, newMetric, cacheTtlHours, TimeUnit.HOURS);
                log.debug("Storing first metric for {}", cacheKey);
                return true;
            }

            long currentLag = newMetric.getProducedOffset() - newMetric.getConsumedOffset();
            long previousLag = previous.getProducedOffset() - previous.getConsumedOffset();

            // Check if lag changed significantly
            boolean lagChanged = hasSignificantLagChange(previousLag, currentLag);

            // Check if minimum time elapsed
            boolean timeElapsed = ChronoUnit.MINUTES.between(
                    previous.getSnapshotTime(),
                    newMetric.getSnapshotTime()) >= minIntervalMinutes;

            if (lagChanged || timeElapsed) {
                redisTemplate.opsForValue().set(cacheKey, newMetric, cacheTtlHours, TimeUnit.HOURS);
                if (lagChanged) {
                    log.debug("Storing metric for {} - lag changed from {} to {}", cacheKey, previousLag, currentLag);
                } else {
                    log.debug("Storing metric for {} - {} minutes elapsed", cacheKey, minIntervalMinutes);
                }
                return true;
            }

            log.trace("Skipping metric for {} - no significant change (lag: {}, prev: {})", cacheKey, currentLag,
                    previousLag);
            return false;

        } catch (Exception ex) {
            log.error("Redis cache error for key {}, storing metric as fallback", cacheKey, ex);
            // Fail-safe: store metric if Redis fails
            return true;
        }
    }

    // Check if lag changed beyond threshold
    private boolean hasSignificantLagChange(long previousLag, long currentLag) {
        // If previous lag was 0, any non-zero lag is significant
        if (previousLag == 0) {
            return currentLag > 0;
        }

        // If current lag is 0 and previous wasn't, that's significant
        if (currentLag == 0 && previousLag > 0) {
            return true;
        }

        // Calculate percentage change
        double change = Math.abs(currentLag - previousLag) / (double) previousLag;
        return change > lagChangeThreshold;
    }

    // Build Redis cache key for topic+partition+consumer group
    private String buildCacheKey(RttmQueueMetricEntity metric) {
        return CACHE_KEY_PREFIX +
                metric.getTopicName() + ":" +
                metric.getPartitionId() + ":" +
                metric.getConsumerGroup();
    }

    // Clear all cached metrics from Redis (useful for testing or manual reset)
    public void clearCache() {
        try {
            var keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Cleared {} cached metric entries from Redis", keys.size());
            }
        } catch (Exception ex) {
            log.error("Failed to clear Redis cache", ex);
        }
    }

    // Get current cache size for monitoring
    public int getCacheSize() {
        try {
            var keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
            return keys != null ? keys.size() : 0;
        } catch (Exception ex) {
            log.error("Failed to get cache size from Redis", ex);
            return -1;
        }
    }
}
