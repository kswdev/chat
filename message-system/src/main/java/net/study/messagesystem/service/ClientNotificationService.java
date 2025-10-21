package net.study.messagesystem.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.outbound.*;
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
        pushService.registerPushMessageType(MessageType.NOTIFY_JOIN, JoinNotification.class);
        pushService.registerPushMessageType(MessageType.NOTIFY_ACCEPT, AcceptNotification.class);
        pushService.registerPushMessageType(MessageType.JOIN_RESPONSE, JoinResponse.class);
        pushService.registerPushMessageType(MessageType.INVITE_RESPONSE, InviteResponse.class);
        pushService.registerPushMessageType(MessageType.ACCEPT_RESPONSE, AcceptResponse.class);
        pushService.registerPushMessageType(MessageType.ASK_INVITE, InviteNotification.class);
        pushService.registerPushMessageType(MessageType.DISCONNECT_RESPONSE, DisconnectResponse.class);
        pushService.registerPushMessageType(MessageType.REJECT_RESPONSE, RejectResponse.class);
        pushService.registerPushMessageType(MessageType.CREATE_RESPONSE, CreateResponse.class);
        pushService.registerPushMessageType(MessageType.QUIT_RESPONSE, QuitResponse.class);
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
