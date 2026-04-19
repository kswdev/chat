package net.study.messageconnectionflux.adpter.in.websocket.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.IdKey;
import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.application.dto.kafka.WriteMessageRecord;
import net.study.messageconnectionflux.application.dto.websocket.inbound.WriteMessage;
import net.study.messageconnectionflux.application.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnectionflux.application.port.out.ClientNotificationService;
import net.study.messageconnectionflux.application.port.out.EventProducer;
import net.study.messageconnectionflux.application.port.out.MessageSeqIdGenerator;
import net.study.messageconnectionflux.domain.channel.ChannelId;
import net.study.messageconnectionflux.domain.message.MessageSeqId;
import net.study.messageconnectionflux.domain.user.UserId;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class WriteMessageHandler implements BaseRequestHandler<WriteMessage> {

    private final EventProducer kafkaProducer;
    private final ClientNotificationService clientNotificationService;
    private final MessageSeqIdGenerator messageSeqIdGenerator;

    @Override
    public Mono<Void> handleRequest(WebSocketSession senderSession, WriteMessage request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        ChannelId channelId = request.getChannelId();
        Runnable errorCallback = () -> clientNotificationService.sendError(senderUserId, new ErrorResponse(MessageType.WRITE_MESSAGE, "Write message failed."));

        return messageSeqIdGenerator
                .getNextMessageSeqId(channelId)
                .flatMap(sequenceId -> sendKafkaMessageEvent(request, channelId, senderUserId, sequenceId, errorCallback));
    }

    private Mono<Void> sendKafkaMessageEvent(WriteMessage request, ChannelId channelId, UserId senderUserId, MessageSeqId messageSeqId, Runnable errorCallback) {
        return kafkaProducer.sendMessageUsingPartitionKey(
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
