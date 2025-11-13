package net.study.messageconnection.handler.websocket;

import lombok.RequiredArgsConstructor;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.websocket.inbound.KeepAliveRequest;
import net.study.messageconnection.handler.websocket.BaseRequestHandler;
import net.study.messageconnection.service.SessionService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class KeepAliveRequestHandler implements BaseRequestHandler<KeepAliveRequest> {

    private final SessionService sessionService;

    @Override
    public void handleRequest(WebSocketSession senderSession, KeepAliveRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        sessionService.refreshTTL(
                senderUserId,
                (String) senderSession.getAttributes().get(IdKey.HTTP_SESSION_ID.getValue())
        );
    }

    @Override
    public Class<KeepAliveRequest> getRequestType() {
        return KeepAliveRequest.class;
    }
}
