package net.study.messageconnectionflux.adpter.in.websocket.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.IdKey;
import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.application.dto.kafka.RejectRequestRecord;
import net.study.messageconnectionflux.application.dto.websocket.inbound.RejectRequest;
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
public class RejectRequestHandler implements BaseRequestHandler<RejectRequest> {

    private final EventProducer eventProducer;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Mono<Void> handleRequest(WebSocketSession senderSession, RejectRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        return eventProducer.sendRequest(
                new RejectRequestRecord(senderUserId, request.getUsername()),
                () -> clientNotificationService.sendError(senderUserId, new ErrorResponse(MessageType.REJECT_REQUEST, "Reject failed.")));
    }

    @Override
    public Class<RejectRequest> getRequestType() {
        return RejectRequest.class;
    }
}
