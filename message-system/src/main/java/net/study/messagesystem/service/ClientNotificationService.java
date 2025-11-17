package net.study.messagesystem.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.*;
import net.study.messagesystem.kafka.KafkaProducer;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientNotificationService {

    private final SessionService sessionService;
    private final KafkaProducer kafkaProducer;
    private final PushService pushService;

    @PostConstruct
    private void init() {
        pushService.registerPushMessageType(MessageType.NOTIFY_JOIN, JoinNotificationRecord.class);
        pushService.registerPushMessageType(MessageType.NOTIFY_ACCEPT, AcceptNotificationRecord.class);
        pushService.registerPushMessageType(MessageType.JOIN_RESPONSE, JoinResponseRecord.class);
        pushService.registerPushMessageType(MessageType.INVITE_RESPONSE, InviteResponseRecord.class);
        pushService.registerPushMessageType(MessageType.ACCEPT_RESPONSE, AcceptResponseRecord.class);
        pushService.registerPushMessageType(MessageType.ASK_INVITE, InviteNotificationRecord.class);
        pushService.registerPushMessageType(MessageType.DISCONNECT_RESPONSE, DisconnectResponseRecord.class);
        pushService.registerPushMessageType(MessageType.REJECT_RESPONSE, RejectResponseRecord.class);
        pushService.registerPushMessageType(MessageType.CREATE_RESPONSE, CreateResponseRecord.class);
        pushService.registerPushMessageType(MessageType.QUIT_RESPONSE, QuitResponseRecord.class);
    }

    public void sendMessage(UserId userId, RecordInterface recordInterface) {
        sessionService
                .getListenTopic(userId)
                .ifPresentOrElse(
                        topic -> kafkaProducer.sendResponse(topic, recordInterface),
                        () -> pushService.pushMessage(recordInterface));
    }

    public void sendMessageUsingPartitionKey(UserId userId, ChannelId channelId, RecordInterface recordInterface) {
        sessionService
                .getListenTopic(userId)
                .ifPresentOrElse(
                        topic -> kafkaProducer.sendMessageUsingPartitionKey(topic, channelId, userId, recordInterface),
                        () -> pushService.pushMessage(recordInterface));
    }

    public void sendError(ErrorResponseRecord errorResponseRecord) {
        sessionService
                .getListenTopic(errorResponseRecord.userId())
                .ifPresentOrElse(
                        topic -> kafkaProducer.sendResponse(topic, errorResponseRecord),
                        () -> log.warn("Send error failed. type: {}, error: {}, user: {}",
                                errorResponseRecord.messageType(),
                                errorResponseRecord.message(),
                                errorResponseRecord.userId()
                        ));
    }

}
