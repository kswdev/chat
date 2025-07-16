package net.study.messagesystem.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.dto.message.Message;
import net.study.messagesystem.dto.user.UserId;
import net.study.messagesystem.dto.websocket.outbound.BaseMessage;
import net.study.messagesystem.util.JsonUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.function.ThrowingConsumer;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSessionManager {

    private final Map<UserId, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final JsonUtil jsonUtil;

    public void sendMessage(WebSocketSession session, BaseMessage message) {
        jsonUtil.toJson(message).ifPresent(msg -> {
            try {
                session.sendMessage(new TextMessage(msg));
                log.info("Send message: [{}] to {}", msg, session.getId());
            } catch (Exception e) {
                log.error("메세지 전송 실패. cause: {}", e.getMessage());
            }
        });
    }

    public List<WebSocketSession> getSessions() {
        return sessions.values().stream().toList();
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
