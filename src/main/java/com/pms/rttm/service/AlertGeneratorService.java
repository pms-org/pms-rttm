package com.pms.rttm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertGeneratorService {

    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random();

    @Scheduled(fixedRate = 30000)
    public void generateSampleAlerts() {
        try {
            String[] metrics = {"kafka_lag", "dlq_count", "latency_spike", "partition_rebalance"};
            String[] services = {"P2", "ENRICHED", "VALIDATED", "COMMITTED"};
            String[] severities = {"HIGH", "MEDIUM", "LOW"};
            
            if (random.nextInt(10) < 3) { // 30% chance
                String metric = metrics[random.nextInt(metrics.length)];
                String service = services[random.nextInt(services.length)];
                String severity = severities[random.nextInt(severities.length)];
                
                String sql = """
                    INSERT INTO rttm_alerts 
                    (metric_name, service_name, current_value, threshold_value, severity, triggered_time, status)
                    VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE')
                    """;
                
                jdbcTemplate.update(sql, metric, service, 
                                  random.nextDouble() * 100, 
                                  random.nextDouble() * 50,
                                  severity, 
                                  LocalDateTime.now());
                
                log.debug("Generated sample alert: {} - {} - {}", severity, metric, service);
            }
        } catch (Exception e) {
            log.error("Failed to generate sample alert", e);
        }
    }
}