package net.study.messageconnection.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.kafka.FetchUserInviteCodeRequestRecord;
import net.study.messageconnection.dto.websocket.inbound.FetchUserInviteCodeRequest;
import net.study.messageconnection.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnection.kafka.KafkaProducer;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchUserInviteCodeRequestHandler implements BaseRequestHandler<FetchUserInviteCodeRequest> {

    private final KafkaProducer kafkaProducer;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, FetchUserInviteCodeRequest request) {
        UserId requestUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        kafkaProducer.sendRequest(
                new FetchUserInviteCodeRequestRecord(requestUserId),
                () -> clientNotificationService.sendError(senderSession, new ErrorResponse(MessageType.FETCH_USER_INVITE_CODE_REQUEST, "fetch user invite code failed.")));
    }
    @Override
    public Class<FetchUserInviteCodeRequest> getRequestType() {
        return FetchUserInviteCodeRequest.class;
    }
}
