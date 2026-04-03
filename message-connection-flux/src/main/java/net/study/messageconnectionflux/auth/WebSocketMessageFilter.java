package net.study.messageconnectionflux.auth;

import net.study.messagecommon.constant.IdKey;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@Order(-1)
public class WebSocketMessageFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        if (isWebSocketRequest(exchange) && isTargetPath(exchange)) {

            String token = exchange.getRequest()
                    .getHeaders()
                    .getFirst("Authorization");

            if (token == null) {
                return Mono.error(new RuntimeException("인증 실패"));
            }

            // 검증 + attribute 저장
            exchange.getAttributes().put(IdKey.USER_ID.getValue(), getUserId(exchange));
        }

        return chain.filter(exchange);
    }

    private boolean isWebSocketRequest(ServerWebExchange exchange) {
        String upgrade = exchange.getRequest().getHeaders().getUpgrade();
        return "websocket".equalsIgnoreCase(upgrade);
    }

    private boolean isTargetPath(ServerWebExchange exchange) {
        return exchange.getRequest().getURI().getPath().startsWith("/ws/v1/message");
    }

    private Optional<Long> getUserId(ServerWebExchange exchange) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-Authorization-Id");
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.of(Long.valueOf(userId));
    }
}
