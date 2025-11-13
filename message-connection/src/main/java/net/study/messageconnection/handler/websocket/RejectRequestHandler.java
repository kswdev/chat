package net.study.messageconnection.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.constant.UserConnectionStatus;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.websocket.inbound.RejectRequest;
import net.study.messageconnection.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnection.dto.websocket.outbound.RejectResponse;
import net.study.messageconnection.service.ClientNotificationService;
import net.study.messageconnection.service.UserConnectionService;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class RejectRequestHandler implements BaseRequestHandler<RejectRequest> {

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, RejectRequest request) {
        UserId rejecterUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        Pair<Boolean, String> result = userConnectionService.reject(rejecterUserId, request.getUsername());

        if (result.getFirst()) {
            String inviterUsername = result.getSecond();
            clientNotificationService.sendError(senderSession, rejecterUserId, new RejectResponse(inviterUsername, UserConnectionStatus.REJECTED));
        } else {
            String errorMessage = result.getSecond();
            clientNotificationService.sendError(senderSession, rejecterUserId, new ErrorResponse(errorMessage, MessageType.REJECT_REQUEST));
        }
    }

    @Override
    public Class<RejectRequest> getRequestType() {
        return RejectRequest.class;
    }
}
