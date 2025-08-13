package net.study.messagesystem.config;

import lombok.RequiredArgsConstructor;
import net.study.messagesystem.auth.CustomHttpSessionHandshakeInterceptor;

import net.study.messagesystem.handler.WebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketHandlerConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;
    private final CustomHttpSessionHandshakeInterceptor customHttpSessionHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws/v1/message")
                .addInterceptors(customHttpSessionHandshakeInterceptor);
    }
}
