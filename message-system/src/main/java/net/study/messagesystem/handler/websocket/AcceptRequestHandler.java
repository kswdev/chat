package net.study.messagesystem.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.IdKey;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.domain.user.UserId;
import net.study.messagesystem.dto.websocket.inbound.AcceptRequest;
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
public class AcceptRequestHandler implements BaseRequestHandler<AcceptRequest> {

    private final UserConnectionService userConnectionService;
    private final WebSocketSessionManager webSocketSessionManager;

    @Override
    public void handleRequest(WebSocketSession senderSession, AcceptRequest request) {
        UserId accepterUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        Pair<Optional<UserId>, String> result = userConnectionService.accept(accepterUserId, request.getUsername());

        result.getFirst()
                .ifPresentOrElse(inviterUserId -> {
                    String accepterUsername = result.getSecond();
                    webSocketSessionManager.sendMessage(senderSession, new AcceptResponse(request.getUsername()));
                    webSocketSessionManager.sendMessage(
                            webSocketSessionManager.getSession(inviterUserId), new AcceptNotification(accepterUsername)
                    );
                }, () -> {
                    String errorMessage = result.getSecond();
                    webSocketSessionManager.sendMessage(senderSession, new ErrorResponse(errorMessage, MessageType.ACCEPT_REQUEST));
                });
    }

    @Override
    public Class<AcceptRequest> getRequestType() {
        return AcceptRequest.class;
    }
}
