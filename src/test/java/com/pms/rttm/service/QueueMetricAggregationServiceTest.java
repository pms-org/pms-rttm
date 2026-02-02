package com.pms.rttm.service;

import com.pms.rttm.entity.RttmQueueMetricEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Redis-based queue metric aggregation.
 * Tests the aggregation logic with actual Redis connection.
 */
@SpringBootTest
@ActiveProfiles("test")
class QueueMetricAggregationServiceTest {

    @Autowired
    private QueueMetricAggregationService aggregationService;

    @Autowired
    private RedisTemplate<String, RttmQueueMetricEntity> redisTemplate;

    @BeforeEach
    void setUp() {
        // Clear cache before each test
        aggregationService.clearCache();
    }

    @Test
    void shouldStoreFirstMetric() {
        RttmQueueMetricEntity metric = buildMetric("topic1", 0, "group1", 100, 50, Instant.now());

        boolean shouldStore = aggregationService.shouldStore(metric);

        assertTrue(shouldStore, "First metric should always be stored");
    }

    @Test
    void shouldSkipDuplicateMetricWithNoChange() {
        Instant now = Instant.now();
        RttmQueueMetricEntity metric1 = buildMetric("topic1", 0, "group1", 100, 50, now);
        RttmQueueMetricEntity metric2 = buildMetric("topic1", 0, "group1", 100, 50, now.plusSeconds(30));

        assertTrue(aggregationService.shouldStore(metric1), "First metric should be stored");
        assertFalse(aggregationService.shouldStore(metric2), "Duplicate metric with no lag change should be skipped");
    }

    @Test
    void shouldStoreWhenLagChangesSignificantly() {
        Instant now = Instant.now();
        RttmQueueMetricEntity metric1 = buildMetric("topic1", 0, "group1", 100, 50, now);
        RttmQueueMetricEntity metric2 = buildMetric("topic1", 0, "group1", 200, 50, now.plusSeconds(30));

        assertTrue(aggregationService.shouldStore(metric1), "First metric should be stored");
        assertTrue(aggregationService.shouldStore(metric2), "Metric with >10% lag change should be stored");
    }

    @Test
    void shouldStoreWhenTimeIntervalElapsed() {
        Instant now = Instant.now();
        RttmQueueMetricEntity metric1 = buildMetric("topic1", 0, "group1", 100, 50, now);
        RttmQueueMetricEntity metric2 = buildMetric("topic1", 0, "group1", 100, 50, now.plus(6, ChronoUnit.MINUTES));

        assertTrue(aggregationService.shouldStore(metric1), "First metric should be stored");
        assertTrue(aggregationService.shouldStore(metric2), "Metric after 5+ minutes should be stored");
    }

    @Test
    void shouldHandleZeroLag() {
        Instant now = Instant.now();
        RttmQueueMetricEntity metric1 = buildMetric("topic1", 0, "group1", 100, 100, now);
        RttmQueueMetricEntity metric2 = buildMetric("topic1", 0, "group1", 150, 100, now.plusSeconds(30));

        assertTrue(aggregationService.shouldStore(metric1), "First metric (zero lag) should be stored");
        assertTrue(aggregationService.shouldStore(metric2), "Any lag after zero lag should be stored");
    }

    @Test
    void shouldHandleDifferentTopicsIndependently() {
        Instant now = Instant.now();
        RttmQueueMetricEntity metric1 = buildMetric("topic1", 0, "group1", 100, 50, now);
        RttmQueueMetricEntity metric2 = buildMetric("topic2", 0, "group1", 100, 50, now.plusSeconds(30));

        assertTrue(aggregationService.shouldStore(metric1), "Metric for topic1 should be stored");
        assertTrue(aggregationService.shouldStore(metric2), "Metric for topic2 should be stored (different topic)");
    }

    @Test
    void shouldGetCacheSize() {
        Instant now = Instant.now();
        aggregationService.shouldStore(buildMetric("topic1", 0, "group1", 100, 50, now));
        aggregationService.shouldStore(buildMetric("topic2", 0, "group1", 100, 50, now));
        aggregationService.shouldStore(buildMetric("topic1", 1, "group1", 100, 50, now));

        int cacheSize = aggregationService.getCacheSize();
        assertEquals(3, cacheSize, "Cache should contain 3 unique entries");
    }

    private RttmQueueMetricEntity buildMetric(String topic, int partition, String group,
            long producedOffset, long consumedOffset, Instant snapshotTime) {
        return RttmQueueMetricEntity.builder()
                .serviceName("test-service")
                .topicName(topic)
                .partitionId(partition)
                .producedOffset(producedOffset)
                .consumedOffset(consumedOffset)
                .consumerGroup(group)
                .snapshotTime(snapshotTime)
                .build();
    }
}
