package net.study.messageconnection.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.domain.user.UserId;
import org.springframework.stereotype.Component;
import org.springframework.util.function.ThrowingConsumer;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSessionManager {

    private final Map<UserId, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void sendMessage(WebSocketSession session, String message) throws IOException {
        try {
            session.sendMessage(new TextMessage(message));
            log.info("Send message: [{}] to {}", message, session.getId());
        } catch (IOException e) {
            log.error("Send Message failed. cause: {}", e.getMessage());
            throw e;
        }
    }

    public WebSocketSession getSession(UserId userId) {
        return sessions.get(userId);
    }

    public void putSession(UserId userId, WebSocketSession session) {
        sessions.put(userId, session);
        log.info("Session stored: {}", session.getId());
    }

    public void closeSession(UserId userId) {
        log.info("Session removed: {}", userId);
        Optional<WebSocketSession> webSocketSession = Optional.ofNullable(sessions.remove(userId));
        webSocketSession.ifPresent(throwingConsumerWrapper(WebSocketSession::close));
    }

    public <T> Consumer<T> throwingConsumerWrapper(ThrowingConsumer<T> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception e) {
                log.error("Faield WebSocketSession close. : {}", e.getMessage());
            }
        };
    }
}
