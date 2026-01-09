package com.pms.rttm.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.rttm.service.RttmDashboardService;
import com.pms.rttm.websocket.dto.PipelineStage;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PipelineWsHandler extends AbstractRttmWsHandler {

    private final RttmDashboardService dashboardService;

    public PipelineWsHandler(ObjectMapper mapper,
            RttmDashboardService dashboardService) {
        super(mapper);
        this.dashboardService = dashboardService;
    }

    @Override
    protected void sendInitial(WebSocketSession session) {
        broadcast(dashboardService.pipelineStages());
    }

    @Scheduled(fixedRate = 2000)
    public void pushPipeline() {
        List<PipelineStage> pipeline = dashboardService.pipelineStages();
        broadcast(pipeline);
    }
}
