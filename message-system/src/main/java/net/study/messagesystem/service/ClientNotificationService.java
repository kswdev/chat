package net.study.messagesystem.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.domain.user.UserId;
import net.study.messagesystem.dto.websocket.outbound.BaseMessage;
import net.study.messagesystem.session.WebSocketSessionManager;
import net.study.messagesystem.util.JsonUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientNotificationService {

    private final WebSocketSessionManager webSocketSessionManager;
    private final PushService pushService;
    private final JsonUtil jsonUtil;

    @PostConstruct
    private void init() {
        pushService.registerPushMessageType(MessageType.INVITE_RESPONSE);
        pushService.registerPushMessageType(MessageType.ASK_INVITE);
        pushService.registerPushMessageType(MessageType.ACCEPT_RESPONSE);
        pushService.registerPushMessageType(MessageType.NOTIFY_ACCEPT);
        pushService.registerPushMessageType(MessageType.JOIN_RESPONSE);
        pushService.registerPushMessageType(MessageType.NOTIFY_JOIN);
        pushService.registerPushMessageType(MessageType.DISCONNECT_RESPONSE);
        pushService.registerPushMessageType(MessageType.REJECT_RESPONSE);
        pushService.registerPushMessageType(MessageType.CREATE_RESPONSE);
        pushService.registerPushMessageType(MessageType.QUIT_RESPONSE);
    }

    public void sendMessage(WebSocketSession session, UserId userId, BaseMessage message) {
        sendPayload(session, userId, message);
    }

    public void sendMessage(UserId userId, BaseMessage message) {
        sendPayload(webSocketSessionManager.getSession(userId), userId, message);
    }

    public void sendPayload(WebSocketSession session, UserId userId, BaseMessage message) {
        jsonUtil.toJson(message)
                .ifPresentOrElse(payload -> {
                    try {
                        if (session != null)
                            webSocketSessionManager.sendMessage(session, payload);
                        else
                            pushService.pushMessage(userId, message.getType(), payload);
                    } catch (Exception e) {
                        pushService.pushMessage(userId, message.getType(), payload);
                    }
                }, () -> log.error("Failed to send message. messageType: {}", message.getType()));
    }
}
