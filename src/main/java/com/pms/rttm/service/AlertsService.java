package com.pms.rttm.service;

import com.pms.rttm.dto.Alert;
import com.pms.rttm.entity.RttmAlertEntity;
import com.pms.rttm.repository.RttmAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertsService {

    private final RttmAlertRepository alertRepository;

    // Fetch latest alerts by status (e.g. ACTIVE). Maps to Alert DTO.
    public List<Alert> latestByStatus(String status, int limit) {
        Pageable p = PageRequest.of(0, Math.max(1, limit));
        List<RttmAlertEntity> rows = alertRepository.findByStatusOrderByTriggeredTimeDesc(status, p);
        Instant now = Instant.now();
        return rows.stream().map(r -> {
            String svc = (r.getServiceName() == null) ? "" : r.getServiceName();
            String message = String
                    .format("%s %s: %.2f vs %.2f", r.getMetricName(), svc, r.getCurrentValue(), r.getThresholdValue())
                    .trim();
            String timeAgo = formatTimeAgo(Duration.between(r.getTriggeredTime(), now));
            return new Alert(message, r.getSeverity(), timeAgo);
        }).collect(Collectors.toList());
    }

    private String formatTimeAgo(Duration d) {
        if (d.isNegative())
            return "now";
        long seconds = d.getSeconds();
        if (seconds < 60)
            return seconds + "s ago";
        long minutes = seconds / 60;
        if (minutes < 60)
            return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24)
            return hours + "h ago";
        long days = hours / 24;
        return days + "d ago";
    }

}
