package net.study.messageconnection.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.kafka.JoinRequestRecord;
import net.study.messageconnection.dto.websocket.inbound.JoinRequest;
import net.study.messageconnection.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnection.kafka.KafkaProducer;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class JoinRequestHandler implements BaseRequestHandler<JoinRequest> {

    private final KafkaProducer kafkaProducer;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, JoinRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        kafkaProducer.sendRequest(
                new JoinRequestRecord(senderUserId, request.getInviteCode()),
                () -> clientNotificationService.sendError(senderSession, new ErrorResponse(MessageType.JOIN_REQUEST, "join channel failed.")));
    }

    @Override
    public Class<JoinRequest> getRequestType() {
        return JoinRequest.class;
    }
}
