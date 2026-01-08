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

    // private final StatsWebSocketHandler statsHandler;
    // private final LagWebSocketHandler lagHandler;
    // private final DlqWebSocketHandler dlqHandler;
    // private final AlertsWebSocketHandler alertsHandler;
    // private final MetricsWebSocketHandler metricsHandler;
    // private final TrendWebSocketHandler trendHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // registry.addHandler(statsHandler, "/ws/stats").setAllowedOrigins("*");
        // registry.addHandler(lagHandler, "/ws/lag").setAllowedOrigins("*");
        // registry.addHandler(dlqHandler, "/ws/dlq").setAllowedOrigins("*");
        // registry.addHandler(alertsHandler, "/ws/alerts").setAllowedOrigins("*");
        // registry.addHandler(metricsHandler, "/ws/metrics").setAllowedOrigins("*");
        // registry.addHandler(trendHandler, "/ws/trend").setAllowedOrigins("*");
    }
}