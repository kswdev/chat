package net.study.messageconnection.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.kafka.FetchChannelInviteCodeRequestRecord;
import net.study.messageconnection.dto.websocket.inbound.FetchChannelInviteCodeRequest;
import net.study.messageconnection.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnection.kafka.KafkaProducer;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchChannelInviteCodeRequestHandler implements BaseRequestHandler<FetchChannelInviteCodeRequest> {

    private final KafkaProducer kafkaProducer;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, FetchChannelInviteCodeRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        kafkaProducer.sendRequest(
                new FetchChannelInviteCodeRequestRecord(senderUserId, request.getChannelId()),
                () -> clientNotificationService.sendError(senderSession, new ErrorResponse(MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST, "fetch channel invite code failed.")));
    }

    @Override
    public Class<FetchChannelInviteCodeRequest> getRequestType() {
        return FetchChannelInviteCodeRequest.class;
    }
}
