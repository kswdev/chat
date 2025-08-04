package net.study.messagesystem.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.Constants;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.user.UserId;
import net.study.messagesystem.dto.websocket.inbound.AcceptRequest;
import net.study.messagesystem.dto.websocket.inbound.RejectRequest;
import net.study.messagesystem.dto.websocket.outbound.AcceptNotification;
import net.study.messagesystem.dto.websocket.outbound.AcceptResponse;
import net.study.messagesystem.dto.websocket.outbound.ErrorResponse;
import net.study.messagesystem.service.UserConnectionService;
import net.study.messagesystem.session.WebSocketSessionManager;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RejectRequestHandler implements BaseRequestHandler<RejectRequest> {

    private final UserConnectionService userConnectionService;
    private final WebSocketSessionManager webSocketSessionManager;

    @Override
    public void handleRequest(WebSocketSession senderSession, RejectRequest request) {
        UserId accepterUserId = (UserId) senderSession.getAttributes().get(Constants.USER_ID.getValue());

    }

    @Override
    public Class<RejectRequest> getRequestType() {
        return RejectRequest.class;
    }
}
