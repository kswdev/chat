package net.study.messageconnection.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.constant.UserConnectionStatus;
import net.study.messageconnection.domain.connection.Connection;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.websocket.inbound.FetchUserConnectionsRequest;
import net.study.messageconnection.dto.websocket.outbound.FetchUserConnectionsResponse;
import net.study.messageconnection.service.ClientNotificationService;
import net.study.messageconnection.service.UserConnectionService;
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

        clientNotificationService.sendError(senderSession, requestUserId, new FetchUserConnectionsResponse(connections));
    }
    @Override
    public Class<FetchUserConnectionsRequest> getRequestType() {
        return FetchUserConnectionsRequest.class;
    }
}
