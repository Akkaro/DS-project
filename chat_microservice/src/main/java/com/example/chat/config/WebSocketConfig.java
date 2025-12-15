package com.example.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker to send messages back to the client
        // /topic: for broadcasting (e.g., admin seeing all messages)
        // /queue: for private messages (e.g., specific user notifications)
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for messages sent FROM client TO server
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The endpoint the client connects to
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Allow all origins (CORS)
                .withSockJS(); // Enable SockJS fallback options
    }
}