package net.study.messageconnectionflux.adpter.in.websocket.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.IdKey;
import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.application.dto.kafka.InviteRequestRecord;
import net.study.messageconnectionflux.application.dto.websocket.inbound.InviteRequest;
import net.study.messageconnectionflux.application.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnectionflux.application.port.out.ClientNotificationService;
import net.study.messageconnectionflux.application.port.out.EventProducer;
import net.study.messageconnectionflux.domain.user.UserId;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class InviteRequestHandler implements BaseRequestHandler<InviteRequest> {

    private final EventProducer eventProducer;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Mono<Void> handleRequest(WebSocketSession senderSession, InviteRequest request) {
        UserId inviterUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        return eventProducer.sendRequest(
                new InviteRequestRecord(inviterUserId, request.getUserInviteCode()),
                () -> clientNotificationService.sendError(inviterUserId, new ErrorResponse(MessageType.INVITE_REQUEST, "invite request failed.")));
    }

    @Override
    public Class<InviteRequest> getRequestType() {
        return InviteRequest.class;
    }
}
