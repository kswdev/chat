package net.study.messagesystem.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.dto.Message;
import net.study.messagesystem.session.WebSocketSessionManager;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import static java.util.function.Predicate.not;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebSocketSessionManager sessionManager;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        ConcurrentWebSocketSessionDecorator sessionDecorator =
                new ConcurrentWebSocketSessionDecorator(session, 5000, 100 * 1024);
        sessionManager.storeSession(sessionDecorator);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Transport error: [{}], from {}", exception.getMessage(), session.getId());
        sessionManager.removeSession(session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("Disconnected: [{}] from {}", status.toString(), session.getId());
        sessionManager.removeSession(session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession senderSession, TextMessage message) {
        log.info("Received message: [{}] from {}", message.getPayload(), senderSession.getId());
        String payload = message.getPayload();
        try {
            Message receivedMessage = objectMapper.readValue(payload, Message.class);
            sessionManager.getSessions().stream()
                    .filter(not(session -> session.getId().equals(senderSession.getId())))
                    .forEach(participantSession -> sendMessage(participantSession, receivedMessage));

        } catch (Exception e) {
            String errorMessage = "유효한 프로토콜이 아닙니다.";
            log.error("errorMessage payload: {} from {}", payload, senderSession.getId());
            sendMessage(senderSession, new Message("system", errorMessage));
        }
    }

    private void sendMessage(WebSocketSession session, Message message) {
        try {
            String msg = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(msg));
            log.info("Send message: [{}] to {}", msg, session.getId());
        } catch (Exception e) {
            log.error("메세지 전송 실패 to {} error: {}", session.getId(), e.getMessage());
        }
    }
}
