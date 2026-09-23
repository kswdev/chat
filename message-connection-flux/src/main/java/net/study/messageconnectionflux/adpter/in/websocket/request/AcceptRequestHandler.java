package net.study.messageconnectionflux.adpter.in.websocket.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.IdKey;
import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.application.dto.kafka.AcceptRequestRecord;
import net.study.messageconnectionflux.application.dto.websocket.inbound.AcceptRequest;
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
public class AcceptRequestHandler implements BaseRequestHandler<AcceptRequest> {

    private final EventProducer eventProducer;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Mono<Void> handleRequest(WebSocketSession senderSession, AcceptRequest request) {
        var attributes = senderSession.getAttributes();
        UserId accepterUserId = (UserId) attributes.get(IdKey.USER_ID.getValue());
        String accepterUsername = (String) attributes.get(IdKey.USERNAME.getValue());

        return eventProducer.sendRequest(
                new AcceptRequestRecord(accepterUserId, request.getUsername(), accepterUsername),
                () -> clientNotificationService.sendError(accepterUserId, new ErrorResponse(MessageType.ACCEPT_REQUEST, "accept request failed.")));
    }

    @Override
    public Class<AcceptRequest> getRequestType() {
        return AcceptRequest.class;
    }
}
