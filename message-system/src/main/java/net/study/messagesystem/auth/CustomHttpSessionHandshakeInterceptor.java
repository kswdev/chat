package net.study.messagesystem.auth;

import jakarta.servlet.http.HttpSession;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.auth.CustomUserDetails;
import net.study.messagesystem.constant.IdKey;
import net.study.messagesystem.domain.user.UserId;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        Optional<HttpSession> sessionOpt = getHttpSession(request);
        if (sessionOpt.isEmpty()) {
            log.warn("WebSocket handshake failed: HttpSession is null or request type invalid");
            response.setStatusCode(HttpStatus.UNAUTHORIZED); // or BAD_REQUEST depending on case
            return false;
        }

        Optional<Long> userIdOpt = getUserId();
        if (userIdOpt.isEmpty()) {
            log.warn("WebSocket handshake failed: Authentication invalid or Principal not CustomUserDetails");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put(IdKey.HTTP_SESSION_ID.getValue(), sessionOpt.get().getId());
        attributes.put(IdKey.USER_ID.getValue(), new UserId(userIdOpt.get()));
        return true;
    }

    private Optional<HttpSession> getHttpSession(ServerHttpRequest request) {
        return Optional.ofNullable(request)
                .filter(ServletServerHttpRequest.class::isInstance)
                .map(ServletServerHttpRequest.class::cast)
                .map(ServletServerHttpRequest::getServletRequest)
                .map(req -> req.getSession(false));
    }

    private Optional<Long> getUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(CustomUserDetails.class::isInstance)
                .map(CustomUserDetails.class::cast)
                .map(CustomUserDetails::getUserId);
    }
}
