package net.study.messagesystem.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.MessageType;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.*;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientNotificationService {

    private final SessionService sessionService;
    private final RedisNotifier redisNotifier;
    private final PushService pushService;

    @PostConstruct
    private void init() {
        pushService.registerPushMessageType(MessageType.NOTIFY_JOIN, JoinNotificationRecord.class);
        pushService.registerPushMessageType(MessageType.NOTIFY_MESSAGE, MessageNotificationRecord.class);
        pushService.registerPushMessageType(MessageType.JOIN_RESPONSE, JoinResponseRecord.class);
        pushService.registerPushMessageType(MessageType.CREATE_RESPONSE, CreateResponseRecord.class);
        pushService.registerPushMessageType(MessageType.QUIT_RESPONSE, QuitResponseRecord.class);
    }

    public void sendMessage(UserId userId, RecordInterface recordInterface) {
        sessionService
                .getListenTopic(userId)
                .ifPresentOrElse(
                        deliveryChannel -> redisNotifier.publish(deliveryChannel, recordInterface),
                        () -> pushService.pushMessage(recordInterface));
    }

    public void sendMessageUsingPartitionKey(UserId userId, RecordInterface recordInterface) {
        sessionService
                .getListenTopic(userId)
                .ifPresentOrElse(
                        deliveryChannel -> redisNotifier.publish(deliveryChannel, recordInterface),
                        () -> pushService.pushMessage(recordInterface));
    }

    public void sendError(ErrorResponseRecord errorResponseRecord) {
        sessionService
                .getListenTopic(errorResponseRecord.userId())
                .ifPresentOrElse(
                        deliveryChannel -> redisNotifier.publish(deliveryChannel, errorResponseRecord),
                        () -> log.warn("Send error failed. type: {}, error: {}, user: {}",
                                errorResponseRecord.messageType(),
                                errorResponseRecord.message(),
                                errorResponseRecord.userId()
                        ));
    }

}
