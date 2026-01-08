package com.pms.rttm.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.rttm.service.RttmDashboardService;
import com.pms.rttm.websocket.dto.MetricCard;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MetricsWsHandler extends AbstractRttmWsHandler {

    private final RttmDashboardService dashboardService;

    public MetricsWsHandler(ObjectMapper mapper,
            RttmDashboardService dashboardService) {
        super(mapper);
        this.dashboardService = dashboardService;
    }

    @Override
    protected void sendInitial(WebSocketSession session) {
        broadcast(dashboardService.metricCards());
    }

    @Scheduled(fixedRate = 2000)
    public void pushMetrics() {
        List<MetricCard> metrics = dashboardService.metricCards();
        broadcast(metrics);
    }
}
