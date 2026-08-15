package com.hackathon.styling.global.config;

import com.hackathon.styling.domain.styling.websocket.StylingRecommendationWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final StylingRecommendationWebSocketHandler handler;
    private final String[] allowedOrigins;

    public WebSocketConfig(
            StylingRecommendationWebSocketHandler handler,
            @Value("${cors.allowed-origins}") String[] allowedOrigins
    ) {
        this.handler = handler;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/styling")
                .setAllowedOrigins(allowedOrigins);
    }
}
