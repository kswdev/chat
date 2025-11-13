package net.study.messageconnection.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.constant.UserConnectionStatus;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.websocket.inbound.DisconnectRequest;
import net.study.messageconnection.dto.websocket.outbound.DisconnectResponse;
import net.study.messageconnection.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnection.service.ClientNotificationService;
import net.study.messageconnection.service.UserConnectionService;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisconnectRequestHandler implements BaseRequestHandler<DisconnectRequest> {

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, DisconnectRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        Pair<Boolean, String> result = userConnectionService.disconnect(senderUserId, request.getUsername());

        if (result.getFirst()) {
            String partnerUsername = result.getSecond();
            clientNotificationService.sendError(senderSession, senderUserId, new DisconnectResponse(partnerUsername, UserConnectionStatus.DISCONNECTED));
        } else {
            String errorMessage = result.getSecond();
            clientNotificationService.sendError(senderSession, senderUserId, new ErrorResponse(errorMessage, MessageType.DISCONNECT_REQUEST));
        }
    }

    @Override
    public Class<DisconnectRequest> getRequestType() {
        return DisconnectRequest.class;
    }
}
