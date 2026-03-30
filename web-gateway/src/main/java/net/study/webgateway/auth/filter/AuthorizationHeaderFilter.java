package net.study.webgateway.auth.filter;

import net.study.webgateway.auth.jwt.JwtUtils;
import net.study.webgateway.auth.jwt.TokenUser;
import net.study.webgateway.auth.role.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Slf4j
@Component
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    private final JwtUtils jwtUtils;

    public static class Config {}


    public AuthorizationHeaderFilter(JwtUtils jwtUtils) {
        super(Config.class);
        this.jwtUtils = jwtUtils;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            if (!containsAuthorization(request))
                return onError(response, "missing authorization header", HttpStatus.BAD_REQUEST);

            String token = extractToken(request);

            if (!jwtUtils.isValid(token))
                return onError(response, "invalid JWT token", HttpStatus.BAD_REQUEST);

            TokenUser tokenUser = jwtUtils.decode(token);

            addAuthorizationHeaders(request, tokenUser);

            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerHttpResponse response, String error, HttpStatus status) {
        response.setStatusCode(status);

        log.error(error);
        return response.setComplete();
    }

    private boolean containsAuthorization(ServerHttpRequest request) {
        return request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION);
    }

    private String extractToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION).get(0);
        return authorization.replace("Bearer ", "");
    }

    private void addAuthorizationHeaders(ServerHttpRequest request, TokenUser tokenUser) {
        request.mutate()
                .header("X-Authorization-Id", tokenUser.getId())
                .header("X-Authorization-Role", tokenUser.getRole().stream()
                        .map(Role::getName)
                        .collect(Collectors.joining(",")))
                .build();
    }
}
