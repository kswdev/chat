package net.study.messagesystem.handler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.Constants;
import net.study.messagesystem.dto.user.UserId;
import net.study.messagesystem.dto.websocket.inbound.BaseRequest;
import net.study.messagesystem.handler.websocket.RequestHandlerDispatcher;
import net.study.messagesystem.session.WebSocketSessionManager;
import net.study.messagesystem.util.JsonUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebsocketHandler extends TextWebSocketHandler {

    private final JsonUtil jsonUtil;
    private final WebSocketSessionManager sessionManager;
    private final RequestHandlerDispatcher requestHandlerDispatcher;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        ConcurrentWebSocketSessionDecorator sessionDecorator =
                new ConcurrentWebSocketSessionDecorator(session, 5000, 100 * 1024);
        UserId userId = getUserId(session);
        sessionManager.putSession(userId, sessionDecorator);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Transport error: [{}], from {}", exception.getMessage(), session.getId());
        sessionManager.closeSession(getUserId(session));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("Disconnected: [{}] from {}", status.toString(), session.getId());
        sessionManager.closeSession(getUserId(session));
    }

    @Override
    protected void handleTextMessage(WebSocketSession senderSession, TextMessage message) {
        log.info("Received message: [{}] from {}", message.getPayload(), senderSession.getId());

        String payload = message.getPayload();
        jsonUtil.fromJson(payload, BaseRequest.class)
                .ifPresent(meg -> requestHandlerDispatcher.dispatch(senderSession, meg));
    }

    private UserId getUserId(WebSocketSession session) {
        return (UserId) session.getAttributes().get(Constants.USER_ID.getValue());
    }
}
