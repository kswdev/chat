package net.study.messageconnection.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.kafka.*;
import net.study.messageconnection.dto.websocket.outbound.BaseMessage;
import net.study.messageconnection.session.WebSocketSessionManager;
import net.study.messageconnection.util.JsonUtil;
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
        pushService.registerPushMessageType(MessageType.NOTIFY_JOIN, JoinNotificationRecord.class);
        pushService.registerPushMessageType(MessageType.NOTIFY_ACCEPT, AcceptNotificationRecord.class);
        pushService.registerPushMessageType(MessageType.ACCEPT_RESPONSE, AcceptResponseRecord.class);
        pushService.registerPushMessageType(MessageType.ASK_INVITE, InviteNotificationRecord.class);
        pushService.registerPushMessageType(MessageType.DISCONNECT_RESPONSE, DisconnectResponseRecord.class);
        pushService.registerPushMessageType(MessageType.REJECT_RESPONSE, RejectResponseRecord.class);
        pushService.registerPushMessageType(MessageType.CREATE_RESPONSE, CreateResponseRecord.class);
        pushService.registerPushMessageType(MessageType.QUIT_RESPONSE, QuitResponseRecord.class);
    }

    public void sendError(WebSocketSession session, BaseMessage message) {
        sendPayload(session, message, null);
    }

    public void sendMessage(UserId userId, BaseMessage message, RecordInterface recordInterface) {
        sendPayload(webSocketSessionManager.getSession(userId), message, recordInterface);
    }

    public void sendPayload(WebSocketSession session, BaseMessage message, RecordInterface recordInterface) {

        Runnable pushMessage = () -> {
            if (recordInterface != null)
                pushService.pushMessage(recordInterface);
        };

        jsonUtil.toJson(message)
                .ifPresentOrElse(payload -> {
                    try {
                        if (session != null)
                            webSocketSessionManager.sendMessage(session, payload);
                        else
                            pushMessage.run();
                    } catch (Exception e) {
                        pushMessage.run();
                    }
                }, () -> log.error("Failed to send message. messageType: {}", message.getType()));
    }
}
