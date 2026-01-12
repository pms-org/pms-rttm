package com.pms.rttm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.pms.rttm.websocket.MetricsWebSocketHandler;
import com.pms.rttm.websocket.PipelineWebSocketHandler;
import com.pms.rttm.websocket.DlqWebSocketHandler;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MetricsWebSocketHandler metricsWebSocketHandler;
    private final PipelineWebSocketHandler pipelineWebSocketHandler;
    private final DlqWebSocketHandler dlqWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(metricsWebSocketHandler, "/ws/rttm/metrics")
                .setAllowedOrigins("*");

        registry.addHandler(pipelineWebSocketHandler, "/ws/rttm/pipeline")
                .setAllowedOrigins("*");

        registry.addHandler(dlqWebSocketHandler, "/ws/rttm/dlq")
                .setAllowedOrigins("*");
    }
}