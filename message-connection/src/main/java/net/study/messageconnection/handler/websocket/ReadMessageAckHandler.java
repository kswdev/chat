package net.study.messageconnection.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.channel.ChannelId;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.kafka.ReadMessageAckRecord;
import net.study.messageconnection.dto.websocket.inbound.ReadMessageAck;
import net.study.messageconnection.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnection.kafka.KafkaProducer;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReadMessageAckHandler implements BaseRequestHandler<ReadMessageAck> {

    private final KafkaProducer kafkaProducer;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, ReadMessageAck request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        ChannelId channelId = request.getChannelId();
        kafkaProducer.sendMessageUsingPartitionKey(
                channelId, senderUserId,
                new ReadMessageAckRecord(senderUserId, channelId, request.getMessageSeqId()),
                () -> clientNotificationService.sendError(senderSession, new ErrorResponse(MessageType.READ_MESSAGE_ACK, "Read message ack failed.")));
    }

    @Override
    public Class<ReadMessageAck> getRequestType() {
        return ReadMessageAck.class;
    }
}
