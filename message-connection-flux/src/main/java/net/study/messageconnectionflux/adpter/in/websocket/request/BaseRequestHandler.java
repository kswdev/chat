package net.study.messageconnectionflux.adpter.in.websocket.request;


import net.study.messageconnectionflux.application.dto.websocket.inbound.BaseRequest;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

public interface BaseRequestHandler<T extends BaseRequest> {
    Class<T> getRequestType();
    Mono<Void> handleRequest(WebSocketSession session, T request);
}
