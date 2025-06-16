package net.study.messagesystem.auth;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomHttpSessionHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        if (request instanceof ServletServerHttpRequest servletServerHttpRequest) {
            HttpSession httpSession = servletServerHttpRequest.getServletRequest().getSession(false);
            if (httpSession != null) {
                attributes.put(Constants.HTTP_SESSION_ID.getValue(), httpSession.getId());
                return true;
            } else {
                log.info("WebSocket handshake failed. httpSession is null");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
        } else {
            log.info("WebSocket handshake failed. request is {}", request.getClass());
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }
    }
}
