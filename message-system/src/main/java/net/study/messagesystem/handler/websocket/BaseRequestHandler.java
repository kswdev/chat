package net.study.messagesystem.handler.websocket;

import net.study.messagesystem.dto.websocket.inbound.BaseRequest;
import org.springframework.web.socket.WebSocketSession;

public interface BaseRequestHandler<T extends BaseRequest> {
    void handleRequest(WebSocketSession session, T request);
    Class<T> getRequestType();
}
