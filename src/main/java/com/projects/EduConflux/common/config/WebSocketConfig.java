package com.projects.EduConflux.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig
        implements WebSocketMessageBrokerConfigurer {

    private final JwtStompInterceptor jwtStompInterceptor;

    @Override
    public void configureMessageBroker(
            MessageBrokerRegistry registry) {

        registry.enableSimpleBroker(
                "/topic",
                "/queue"
        );

        registry.setApplicationDestinationPrefixes(
                "/app"
        );
    }

    @Override
    public void registerStompEndpoints(
            StompEndpointRegistry registry) {

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration) {

        /*
         * 1. JWT interceptor authenticates the STOMP CONNECT
         *    and attaches the Authentication/Principal.
         *
         * 2. SecurityContextChannelInterceptor takes that
         *    Authentication and places it into
         *    SecurityContextHolder while the message is handled.
         */
        registration.interceptors(
                jwtStompInterceptor,
                new SecurityContextChannelInterceptor()
        );
    }
}