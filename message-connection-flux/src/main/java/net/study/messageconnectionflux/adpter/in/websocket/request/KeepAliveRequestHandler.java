package net.study.messageconnectionflux.adpter.in.websocket.request;

import lombok.RequiredArgsConstructor;
import net.study.messagecommon.constant.IdKey;

import net.study.messageconnectionflux.application.dto.websocket.inbound.KeepAliveRequest;
import net.study.messageconnectionflux.application.port.in.SessionService;
import net.study.messageconnectionflux.domain.user.UserId;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
public class KeepAliveRequestHandler implements BaseRequestHandler<KeepAliveRequest> {

    private final SessionService sessionService;

    @Override
    public Mono<Void> handleRequest(WebSocketSession senderSession, KeepAliveRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        return sessionService.refreshTTL(senderUserId).then();
    }

    @Override
    public Class<KeepAliveRequest> getRequestType() {
        return KeepAliveRequest.class;
    }
}
