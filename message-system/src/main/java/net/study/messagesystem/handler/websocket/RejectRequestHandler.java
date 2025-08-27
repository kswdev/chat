package net.study.messagesystem.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.IdKey;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.dto.domain.user.UserId;
import net.study.messagesystem.dto.websocket.inbound.RejectRequest;
import net.study.messagesystem.dto.websocket.outbound.ErrorResponse;
import net.study.messagesystem.dto.websocket.outbound.RejectResponse;
import net.study.messagesystem.service.UserConnectionService;
import net.study.messagesystem.session.WebSocketSessionManager;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class RejectRequestHandler implements BaseRequestHandler<RejectRequest> {

    private final UserConnectionService userConnectionService;
    private final WebSocketSessionManager webSocketSessionManager;

    @Override
    public void handleRequest(WebSocketSession senderSession, RejectRequest request) {
        UserId rejecterUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        Pair<Boolean, String> result = userConnectionService.reject(rejecterUserId, request.getUsername());

        if (result.getFirst()) {
            String inviterUsername = result.getSecond();
            webSocketSessionManager.sendMessage(senderSession, new RejectResponse(inviterUsername, UserConnectionStatus.REJECTED));
        } else {
            String errorMessage = result.getSecond();
            webSocketSessionManager.sendMessage(senderSession, new ErrorResponse(errorMessage, MessageType.REJECT_REQUEST));
        }
    }

    @Override
    public Class<RejectRequest> getRequestType() {
        return RejectRequest.class;
    }
}
