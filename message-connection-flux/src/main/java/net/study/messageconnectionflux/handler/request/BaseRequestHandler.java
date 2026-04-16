package net.study.messageconnectionflux.handler.request;


import net.study.messageconnectionflux.dto.websocket.inbound.BaseRequest;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

public interface BaseRequestHandler<T extends BaseRequest> {
    Class<T> getRequestType();
    Mono<Void> handleRequest(WebSocketSession session, T request);
}
