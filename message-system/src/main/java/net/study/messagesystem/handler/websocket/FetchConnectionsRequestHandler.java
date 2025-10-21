package net.study.messagesystem.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.IdKey;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.domain.connection.Connection;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.websocket.inbound.FetchUserConnectionsRequest;
import net.study.messagesystem.dto.websocket.outbound.FetchUserConnectionsResponse;
import net.study.messagesystem.service.UserConnectionService;
import net.study.messagesystem.service.ClientNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchConnectionsRequestHandler implements BaseRequestHandler<FetchUserConnectionsRequest> {

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, FetchUserConnectionsRequest request) {
        UserId requestUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        UserConnectionStatus status = request.getStatus();

        List<Connection> connections = userConnectionService.getUsersByStatus(requestUserId, status).stream()
                        .map(user -> new Connection(user.username(), status))
                        .toList();

        clientNotificationService.sendMessage(senderSession, requestUserId, new FetchUserConnectionsResponse(connections));
    }
    @Override
    public Class<FetchUserConnectionsRequest> getRequestType() {
        return FetchUserConnectionsRequest.class;
    }
}
