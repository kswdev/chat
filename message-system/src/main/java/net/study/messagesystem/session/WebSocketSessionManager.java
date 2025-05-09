package net.study.messagesystem.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.function.ThrowingConsumer;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
@Component
public class WebSocketSessionManager {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public List<WebSocketSession> getSessions() {
        return sessions.values().stream().toList();
    }

    public void storeSession(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("Session stored: {}", session.getId());
    }

    public void removeSession(String sessionId) {
        log.info("Session removed: {}", sessionId);
        Optional<WebSocketSession> webSocketSession = Optional.ofNullable(sessions.remove(sessionId));
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
