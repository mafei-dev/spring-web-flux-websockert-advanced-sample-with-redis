package com.mafei.websocket.config;

import com.mafei.websocket.service.SWService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.util.Map;

@Configuration
public class HealthSocketConfig {
    @Autowired
    private SWService service;

    @Bean
    public HandlerMapping handlerMapping() {
        Map<String, WebSocketHandler> health = Map.of(
                "health", service
        );
        return new SimpleUrlHandlerMapping(health, -1);
    }
}
