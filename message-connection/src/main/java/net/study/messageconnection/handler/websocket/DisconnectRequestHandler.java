package net.study.messageconnection.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.kafka.DisconnectRequestRecord;
import net.study.messageconnection.dto.websocket.inbound.DisconnectRequest;
import net.study.messageconnection.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnection.kafka.KafkaProducer;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisconnectRequestHandler implements BaseRequestHandler<DisconnectRequest> {

    private final KafkaProducer kafkaProducer;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, DisconnectRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        kafkaProducer.sendRequest(
                new DisconnectRequestRecord(senderUserId, request.getUsername()),
                () -> clientNotificationService.sendError(senderSession, new ErrorResponse(MessageType.DISCONNECT_REQUEST, "disconnect request failed.")));
    }

    @Override
    public Class<DisconnectRequest> getRequestType() {
        return DisconnectRequest.class;
    }
}
