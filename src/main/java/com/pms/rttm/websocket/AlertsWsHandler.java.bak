package com.pms.rttm.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.rttm.service.AlertService;
import com.pms.rttm.websocket.dto.Alert;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AlertsWsHandler extends AbstractRttmWsHandler {

    private final AlertService alertService;

    public AlertsWsHandler(ObjectMapper mapper,
            AlertService alertService) {
        super(mapper);
        this.alertService = alertService;
    }

    @Override
    protected void sendInitial(WebSocketSession session) {
        broadcast(alertService.activeAlerts());
    }

    @Scheduled(fixedRate = 2000)
    public void pushAlerts() {
        List<Alert> alerts = alertService.activeAlerts();
        broadcast(alerts);
    }
}
