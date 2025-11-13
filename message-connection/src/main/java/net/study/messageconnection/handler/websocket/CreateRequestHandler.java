package net.study.messageconnection.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.kafka.CreateRequestRecord;
import net.study.messageconnection.dto.websocket.inbound.CreateRequest;
import net.study.messageconnection.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnection.kafka.KafkaProducer;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateRequestHandler implements BaseRequestHandler<CreateRequest> {

    private final KafkaProducer kafkaProducer;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, CreateRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        kafkaProducer.sendRequest(
                new CreateRequestRecord(senderUserId, request.getTitle(), request.getParticipantUsernames()),
                () -> clientNotificationService.sendError(senderSession, new ErrorResponse(MessageType.CREATE_REQUEST, "create channel failed."))
        );
    }

    @Override
    public Class<CreateRequest> getRequestType() {
        return CreateRequest.class;
    }
}
