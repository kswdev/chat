package net.study.messageconnectionflux.adpter.in.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.IdKey;
import net.study.messageconnectionflux.domain.user.UserId;
import net.study.messageconnectionflux.application.dto.websocket.inbound.BaseRequest;
import net.study.messageconnectionflux.adpter.in.websocket.request.RequestDispatcher;
import net.study.messageconnectionflux.application.port.in.SessionService;
import net.study.messageconnectionflux.adpter.out.persistence.redis.WebSocketSessionManager;
import net.study.messageconnectionflux.util.JsonUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks.Many;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageWebSocketHandler implements WebSocketHandler {

    private final JsonUtil jsonUtil;
    private final RequestDispatcher dispatcher;
    private final WebSocketSessionManager sessionManager;
    private final SessionService sessionService;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        UserId userId = getUserId(session);

        Many<String> sink = sessionManager.create(userId);
        Flux<WebSocketMessage> output = sink
                .asFlux()
                .map(session::textMessage);

        Mono<Void> input = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(payload -> jsonUtil.fromJson(payload, BaseRequest.class))
                .flatMap(request -> dispatcher.dispatch(session, request))

                .doOnError(e -> {
                    closeSession(userId);
                    log.error("Transport error: [{}], from {}", e.getMessage(), session.getId());
                })

                .doFinally(signalType -> {
                    closeSession(userId);
                    log.info("Disconnected: [{}] from {}", signalType, session.getId());
                })

                .then();

        return sessionService.setOnline(userId, true)
                .then(session
                        .send(output)
                        .and(input)
                );
    }

    private void closeSession(UserId userId) {
        sessionService.setOnline(userId, false);
        sessionService.deActiveChannel(userId);
        sessionManager.remove(userId);
    }

    private UserId getUserId(WebSocketSession session) {
        String userId = session.getHandshakeInfo().getHeaders().getFirst(IdKey.USER_ID.getValue());
        assert userId != null;
        UserId currentUserId = new UserId(Long.valueOf(userId));
        session.getAttributes().put(IdKey.USER_ID.getValue(), currentUserId);
        return currentUserId;
    }
}
