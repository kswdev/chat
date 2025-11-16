package net.study.messageconnection.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.kafka.MessageNotificationRecord;
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
        CompletableFuture.runAsync(() ->
                sendToWebSocketOrPush(participantId, notification, record), senderThreadPool);
    }

    private void sendToWebSocketOrPush(UserId participantId, MessageNotification notification, MessageNotificationRecord record) {
        WebSocketSession session = sessionManager.getSession(participantId);

        if (session != null && session.isOpen()) {
            sendWebSocketMessage(session, notification, record);
        } else {
            pushService.pushMessage(record);
        }
    }

    private void sendWebSocketMessage(WebSocketSession session, MessageNotification notification, MessageNotificationRecord record) {
        jsonUtil.toJson(notification)
                .ifPresent(payload -> {
                    try {
                        sessionManager.sendMessage(session, payload);
                    } catch (IOException e) {
                        pushService.pushMessage(record);
                    }
                });
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
