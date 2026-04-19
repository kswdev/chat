package net.study.messageconnectionflux.adpter.out.kafka;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.adpter.out.persistence.redis.WebSocketSessionManager;
import net.study.messageconnectionflux.application.dto.kafka.*;
import net.study.messageconnectionflux.application.dto.websocket.outbound.BaseMessage;
import net.study.messageconnectionflux.application.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnectionflux.application.port.out.ClientNotificationService;
import net.study.messageconnectionflux.domain.user.UserId;
import net.study.messageconnectionflux.util.JsonUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientNotificationPush implements ClientNotificationService {

    private final JsonUtil jsonUtil;
    private final KafkaProducer kafkaProducer;
    private final WebSocketSessionManager sessionManager;

    private final HashMap<String, Class<? extends RecordInterface>> pushMessageTypes = new HashMap<>();

    @PostConstruct
    private void init() {
        pushMessageTypes.put(MessageType.NOTIFY_JOIN, JoinNotificationRecord.class);
        pushMessageTypes.put(MessageType.NOTIFY_ACCEPT, AcceptNotificationRecord.class);
        pushMessageTypes.put(MessageType.ACCEPT_RESPONSE, AcceptResponseRecord.class);
        pushMessageTypes.put(MessageType.ASK_INVITE, InviteNotificationRecord.class);
        pushMessageTypes.put(MessageType.DISCONNECT_RESPONSE, DisconnectResponseRecord.class);
        pushMessageTypes.put(MessageType.REJECT_RESPONSE, RejectResponseRecord.class);
        pushMessageTypes.put(MessageType.CREATE_RESPONSE, CreateResponseRecord.class);
        pushMessageTypes.put(MessageType.QUIT_RESPONSE, QuitResponseRecord.class);
    }

    @Override
    public void sendError(UserId userId, ErrorResponse errorResponse) {
        sendPayload(userId, errorResponse, null);
    }

    @Override
    public void sendMessage(UserId userId, BaseMessage message, RecordInterface recordInterface) {
        sendPayload(userId, message, recordInterface);
    }

    private void sendPayload(UserId userId, BaseMessage message, RecordInterface recordInterface) {
        Runnable pushMessage = () -> {
            if (recordInterface != null) {
                pushMessage(recordInterface);
            }
        };

        jsonUtil.toJson(message)
                .ifPresentOrElse(payload -> {
                    try {
                        if (sessionManager.hasActiveSession(userId))
                            sessionManager.pushMessage(userId, payload);
                        else
                            pushMessage.run();
                    } catch (Exception e) {
                        pushMessage.run();
                    }
                }, () -> log.error("Failed to send message. messageType: {}", message.getType()));
    }

    public void pushMessage(RecordInterface recordInterface) {
        String messageType = recordInterface.type();
        if (pushMessageTypes.containsKey(messageType)) {
            kafkaProducer.sendPushNotification(recordInterface);
        } else {
            log.error("Invalid push message type: {}", messageType);
        }
    }
}
