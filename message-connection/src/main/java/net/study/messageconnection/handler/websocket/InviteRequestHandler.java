package net.study.messageconnection.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.kafka.InviteRequestRecord;
import net.study.messageconnection.dto.websocket.inbound.InviteRequest;
import net.study.messageconnection.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnection.kafka.KafkaProducer;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class InviteRequestHandler implements BaseRequestHandler<InviteRequest> {

    private final KafkaProducer kafkaProducer;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, InviteRequest request) {
        UserId inviterUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        kafkaProducer.sendRequest(
                new InviteRequestRecord(inviterUserId, request.getUserInviteCode()),
                () -> clientNotificationService.sendError(senderSession, new ErrorResponse(MessageType.INVITE_REQUEST, "invite request failed.")));
    }

    @Override
    public Class<InviteRequest> getRequestType() {
        return InviteRequest.class;
    }
}
