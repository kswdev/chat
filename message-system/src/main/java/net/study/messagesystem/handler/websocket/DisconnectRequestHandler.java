package net.study.messagesystem.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.IdKey;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.dto.domain.user.UserId;
import net.study.messagesystem.dto.websocket.inbound.DisconnectRequest;
import net.study.messagesystem.dto.websocket.outbound.DisconnectResponse;
import net.study.messagesystem.dto.websocket.outbound.ErrorResponse;
import net.study.messagesystem.service.UserConnectionService;
import net.study.messagesystem.session.WebSocketSessionManager;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisconnectRequestHandler implements BaseRequestHandler<DisconnectRequest> {

    private final UserConnectionService userConnectionService;
    private final WebSocketSessionManager webSocketSessionManager;

    @Override
    public void handleRequest(WebSocketSession senderSession, DisconnectRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        Pair<Boolean, String> result = userConnectionService.disconnect(senderUserId, request.getUsername());

        if (result.getFirst()) {
            String partnerUsername = result.getSecond();
            webSocketSessionManager.sendMessage(senderSession, new DisconnectResponse(partnerUsername, UserConnectionStatus.DISCONNECTED));
        } else {
            String errorMessage = result.getSecond();
            webSocketSessionManager.sendMessage(senderSession, new ErrorResponse(errorMessage, MessageType.DISCONNECT_REQUEST));
        }
    }

    @Override
    public Class<DisconnectRequest> getRequestType() {
        return DisconnectRequest.class;
    }
}
