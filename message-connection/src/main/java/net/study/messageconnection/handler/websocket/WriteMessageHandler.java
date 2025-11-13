package net.study.messageconnection.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.channel.ChannelId;
import net.study.messageconnection.domain.message.MessageSeqId;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.kafka.WriteMessageRecord;
import net.study.messageconnection.dto.websocket.inbound.WriteMessage;
import net.study.messageconnection.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnection.kafka.KafkaProducer;
import net.study.messageconnection.service.ClientNotificationService;
import net.study.messageconnection.service.MessageSeqIdGenerator;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class WriteMessageHandler implements BaseRequestHandler<WriteMessage> {

    private final KafkaProducer kafkaProducer;
    private final ClientNotificationService clientNotificationService;
    private final MessageSeqIdGenerator messageSeqIdGenerator;

    @Override
    public void handleRequest(WebSocketSession senderSession, WriteMessage request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        ChannelId channelId = request.getChannelId();
        Runnable errorCallback = () -> clientNotificationService.sendError(senderSession, new ErrorResponse(MessageType.WRITE_MESSAGE, "Write message failed."));

        messageSeqIdGenerator
                .getNext(channelId)
                .ifPresentOrElse(sendKafkaMessageEvent(request, channelId, senderUserId, errorCallback), errorCallback);
    }

    private Consumer<MessageSeqId> sendKafkaMessageEvent(WriteMessage request, ChannelId channelId, UserId senderUserId, Runnable errorCallback) {
        return messageSeqId ->
                kafkaProducer.sendMessageUsingPartitionKey(
                        channelId, senderUserId,
                        createWriteMessageRecord(request, channelId, senderUserId, messageSeqId),
                        errorCallback);
    }

    private static WriteMessageRecord createWriteMessageRecord(WriteMessage request, ChannelId channelId, UserId senderUserId, MessageSeqId messageSeqId) {
        return new WriteMessageRecord(senderUserId, channelId, request.getContent(), request.getSerial(), messageSeqId);
    }

    @Override
    public Class<WriteMessage> getRequestType() {
        return WriteMessage.class;
    }
}
