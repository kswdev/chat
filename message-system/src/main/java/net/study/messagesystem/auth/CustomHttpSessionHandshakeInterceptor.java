package net.study.messagesystem.auth;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.session.HttpSessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomHttpSessionHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    private final HttpSessionRepository httpSessionRepository;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        List<String> cookies = request.getHeaders().get("Cookie");
        if (cookies != null) {
            for (String cookie : cookies) {
                if (cookie.contains("JSESSIONID")) {
                    String sessionId = cookie.split("=")[1];
                    HttpSession httpSession = httpSessionRepository.findById(sessionId);
                    if (httpSession != null) {
                        log.info("Connected sessionId: {}", sessionId);
                        return true;
                    }
                }
            }
        }

        log.info("Unauthorized access attempt : ClientIP={}", request.getRemoteAddress());
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }
}
