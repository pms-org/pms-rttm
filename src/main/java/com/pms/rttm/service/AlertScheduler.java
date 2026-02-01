package com.pms.rttm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertScheduler {

    private final AlertGenerationService alertGenerationService;

    @Scheduled(fixedDelayString = "${rttm.alerts.evaluation-interval-ms:60000}")
    public void evaluateAlerts() {
        try {
            log.debug("Alert evaluation scheduler triggered");
            alertGenerationService.evaluateAndGenerateAlerts();
        } catch (Exception ex) {
            log.error("Alert evaluation failed", ex);
        }
    }

    @Scheduled(fixedDelayString = "${rttm.alerts.auto-resolve-interval-ms:300000}")
    public void autoResolveStaleAlerts() {
        try {
            log.debug("Auto-resolve scheduler triggered");
            alertGenerationService.autoResolveStaleAlerts(Duration.ofMinutes(15));
            long resolvedCount = alertGenerationService.autoResolveStaleAlerts(Duration.ofMinutes(15));
            log.info("Auto-resolved {} stale alerts", resolvedCount);
        } catch (Exception ex) {
            log.error("Auto-resolve failed", ex);
        }
    }
}
