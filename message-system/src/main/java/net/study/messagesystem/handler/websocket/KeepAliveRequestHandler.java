package net.study.messagesystem.handler.websocket;

import lombok.RequiredArgsConstructor;
import net.study.messagesystem.constant.IdKey;
import net.study.messagesystem.dto.websocket.inbound.KeepAliveRequest;
import net.study.messagesystem.service.SessionService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class KeepAliveRequestHandler implements BaseRequestHandler<KeepAliveRequest> {

    private final SessionService sessionService;

    @Override
    public void handleRequest(WebSocketSession senderSession, KeepAliveRequest request) {
        sessionService.refreshTTL(
                (String) senderSession.getAttributes().get(IdKey.HTTP_SESSION_ID.getValue())
        );
    }

    @Override
    public Class<KeepAliveRequest> getRequestType() {
        return KeepAliveRequest.class;
    }
}
