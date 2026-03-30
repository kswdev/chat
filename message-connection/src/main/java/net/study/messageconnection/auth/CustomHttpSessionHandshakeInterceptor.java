package net.study.messageconnection.auth;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.IdKey;
import net.study.messageconnection.domain.user.UserId;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomHttpSessionHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @NonNull Map<String, Object> attributes
    ) {
        Optional<Long> userIdOpt = getUserId(request);
        if (userIdOpt.isEmpty()) {
            log.warn("WebSocket handshake failed: Authentication invalid or Principal not CustomUserDetails");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put(IdKey.USER_ID.getValue(), new UserId(userIdOpt.get()));
        return true;
    }

    private Optional<Long> getUserId(ServerHttpRequest request) {
        String userId = request.getHeaders().getFirst("X-Authorization-Id");
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.of(Long.valueOf(userId));
    }
}
