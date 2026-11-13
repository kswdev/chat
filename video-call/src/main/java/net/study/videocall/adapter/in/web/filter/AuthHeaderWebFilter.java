package net.study.videocall.adapter.in.web.filter;

import net.study.messagecommon.constant.IdKey;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

@Component
public class AuthHeaderWebFilter implements WebFilter {

    private static final String TARGET_PATH = "/api/v1/video";
    private static final Pattern USER_ID_PATTERN = Pattern.compile("^[0-9]+$");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (!exchange.getRequest().getURI().getPath().startsWith(TARGET_PATH)) {
            return chain.filter(exchange);
        }

        String userId = exchange.getRequest().getHeaders().getFirst(IdKey.USER_ID.getValue());
        if (userId == null || !USER_ID_PATTERN.matcher(userId).matches()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }
}
