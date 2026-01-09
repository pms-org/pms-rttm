package com.pms.rttm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.pms.rttm.config.WebSocketConfig;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MetricsWsHandler metricsWsHandler;
    private final PipelineWsHandler pipelineWsHandler;
    private final AlertsWsHandler alertsWsHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(metricsWsHandler, "/ws/rttm/metrics")
                .setAllowedOrigins("*");

        registry.addHandler(pipelineWsHandler, "/ws/rttm/pipeline")
                .setAllowedOrigins("*");

        registry.addHandler(alertsWsHandler, "/ws/rttm/alerts")
                .setAllowedOrigins("*");
    }
}
