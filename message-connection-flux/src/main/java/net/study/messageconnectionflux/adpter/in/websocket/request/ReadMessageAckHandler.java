package net.study.messageconnectionflux.adpter.in.websocket.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.IdKey;
import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.application.dto.kafka.ReadMessageAckRecord;
import net.study.messageconnectionflux.application.dto.websocket.inbound.ReadMessageAck;
import net.study.messageconnectionflux.application.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnectionflux.application.port.out.ClientNotificationService;
import net.study.messageconnectionflux.application.port.out.EventProducer;
import net.study.messageconnectionflux.domain.channel.ChannelId;
import net.study.messageconnectionflux.domain.user.UserId;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;


@Slf4j
@Component
@RequiredArgsConstructor
public class ReadMessageAckHandler implements BaseRequestHandler<ReadMessageAck> {

    private final EventProducer eventProducer;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Mono<Void> handleRequest(WebSocketSession senderSession, ReadMessageAck request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        ChannelId channelId = request.getChannelId();
        return eventProducer.sendMessageUsingPartitionKey(
                channelId, senderUserId,
                new ReadMessageAckRecord(senderUserId, channelId, request.getMessageSeqId()),
                () -> clientNotificationService.sendError(senderUserId, new ErrorResponse(MessageType.READ_MESSAGE_ACK, "Read message ack failed.")));
    }

    @Override
    public Class<ReadMessageAck> getRequestType() {
        return ReadMessageAck.class;
    }
}
