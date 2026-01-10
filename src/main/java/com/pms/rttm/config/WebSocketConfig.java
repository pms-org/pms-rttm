package com.pms.rttm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.pms.rttm.websocket.MetricsWebSocketHandler;
import com.pms.rttm.websocket.PipelineWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new MetricsWebSocketHandler(), "/ws/rttm/metrics")
                .setAllowedOrigins("*");
        
        registry.addHandler(new PipelineWebSocketHandler(), "/ws/rttm/pipeline")
                .setAllowedOrigins("*");
    }
}