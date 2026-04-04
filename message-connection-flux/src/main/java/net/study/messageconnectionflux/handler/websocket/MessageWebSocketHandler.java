package net.study.messageconnectionflux.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.IdKey;
import net.study.messageconnectionflux.domain.user.UserId;
import net.study.messageconnectionflux.dto.websocket.inbound.BaseRequest;
import net.study.messageconnectionflux.handler.request.RequestDispatcher;
import net.study.messageconnectionflux.util.JsonUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageWebSocketHandler implements WebSocketHandler {

    private final JsonUtil jsonUtil;
    private final RequestDispatcher dispatcher;


    @Override
    public Mono<Void> handle(WebSocketSession session) {
        UserId userId = getUserId(session);

        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(payload -> jsonUtil.fromJson(payload, BaseRequest.class))
                .flatMap(request -> dispatcher.dispatch(session, request))

                .doOnError(e -> {
                    log.error("Transport error: [{}], from {}", e.getMessage(), session.getId());
                })

                .doFinally(signalType -> {
                    log.info("Disconnected: [{}] from {}", signalType, session.getId());
                })

                .then();
    }
    private UserId getUserId(WebSocketSession session) {
        return (UserId) session.getHandshakeInfo()
                .getAttributes()
                .get(IdKey.USER_ID.getValue());
    }
}
