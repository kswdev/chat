package net.study.messageconnection.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.kafka.MessageNotificationRecord;
import net.study.messageconnection.dto.websocket.outbound.BaseMessage;
import net.study.messageconnection.dto.websocket.outbound.MessageNotification;
import net.study.messageconnection.session.WebSocketSessionManager;
import net.study.messageconnection.util.JsonUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final JsonUtil jsonUtil;
    private final PushService pushService;
    private final WebSocketSessionManager sessionManager;
    private final ExecutorService senderThreadPool = Executors.newFixedThreadPool(10);

    public void sendMessage(MessageNotificationRecord messageNotificationRecord) {
        MessageNotification notification = createNotificationMessage(messageNotificationRecord);

        messageNotificationRecord.participantIds()
                .forEach(participantId -> sendMessageAsync(participantId, notification, messageNotificationRecord));
    }

    private void sendMessageAsync(UserId participantId, MessageNotification notification, MessageNotificationRecord record) {
        CompletableFuture.runAsync(() -> {
            try {
                sendToWebSocketOrPush(participantId, notification, record);
            } catch (Exception e) {
                log.error("Failed to send message to user {}", participantId, e);
                handleMessageFailure(record);
            }
        }, senderThreadPool);
    }

    private void sendToWebSocketOrPush(UserId participantId, MessageNotification notification, MessageNotificationRecord record) {
        WebSocketSession session = sessionManager.getSession(participantId);

        if (session != null && session.isOpen()) {
            sendWebSocketMessage(session, notification, participantId, record);
        } else {
            handleMessageFailure(record);
        }
    }

    private void sendWebSocketMessage(WebSocketSession session, MessageNotification notification, UserId participantId, MessageNotificationRecord record) {
        String payload = convertToJson(notification);
        if (payload == null) {
            log.error("Failed to convert message to JSON for user {}", participantId);
            handleMessageFailure(record);
            return;
        }

        try {
            sessionManager.sendMessage(session, payload);
        } catch (IOException e) {
            log.warn("WebSocket message failed for user {}, falling back to push notification", participantId, e);
            handleMessageFailure(record);
        }
    }

    private String convertToJson(BaseMessage message) {
        return jsonUtil.toJson(message)
                .orElseGet(() -> {
                    log.error("Failed to serialize message. messageType: {}", message.getType());
                    return null;
                });
    }

    private void handleMessageFailure(MessageNotificationRecord record) {
        pushService.pushMessage(record);
    }

    private MessageNotification createNotificationMessage(MessageNotificationRecord record) {
        return new MessageNotification(
                record.channelId(),
                record.messageSeqId(),
                record.username(),
                record.content()
        );
    }
}
