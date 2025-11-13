package net.study.messageconnection.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.kafka.FetchMessagesRequestRecord;
import net.study.messageconnection.dto.websocket.inbound.FetchMessagesRequest;
import net.study.messageconnection.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnection.kafka.KafkaProducer;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchMessageRequestHandler implements BaseRequestHandler<FetchMessagesRequest> {

    private final KafkaProducer kafkaProducer;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, FetchMessagesRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        kafkaProducer.sendMessageUsingPartitionKey(
                request.getChannelId(), senderUserId,
                new FetchMessagesRequestRecord(senderUserId, request.getChannelId(), request.getStartMessageSeqId(), request.getEndMessageSeqId()),
                () -> clientNotificationService.sendError(senderSession, new ErrorResponse(MessageType.FETCH_MESSAGES_REQUEST, "fetch messages request failed")));
    }

    @Override
    public Class<FetchMessagesRequest> getRequestType() {
        return FetchMessagesRequest.class;
    }
}
