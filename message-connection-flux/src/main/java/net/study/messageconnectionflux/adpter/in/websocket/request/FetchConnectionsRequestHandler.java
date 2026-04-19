package net.study.messageconnectionflux.adpter.in.websocket.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.IdKey;
import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.application.dto.kafka.FetchUserConnectionsRequestRecord;
import net.study.messageconnectionflux.application.dto.websocket.inbound.FetchUserConnectionsRequest;
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
public class FetchConnectionsRequestHandler implements BaseRequestHandler<FetchUserConnectionsRequest> {

    private final EventProducer eventProducer;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Mono<Void> handleRequest(WebSocketSession senderSession, FetchUserConnectionsRequest request) {
        UserId requestUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        return eventProducer.sendRequest(
                new FetchUserConnectionsRequestRecord(requestUserId, request.getStatus()),
                () -> clientNotificationService.sendError(requestUserId, new ErrorResponse(MessageType.FETCH_USER_CONNECTIONS_REQUEST, "fetch user connections failed.")));
    }
    @Override
    public Class<FetchUserConnectionsRequest> getRequestType() {
        return FetchUserConnectionsRequest.class;
    }
}
