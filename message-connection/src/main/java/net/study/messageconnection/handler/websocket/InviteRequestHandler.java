package net.study.messageconnection.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.constant.UserConnectionStatus;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.websocket.inbound.InviteRequest;
import net.study.messageconnection.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnection.dto.websocket.outbound.InviteNotification;
import net.study.messageconnection.dto.websocket.outbound.InviteResponse;
import net.study.messageconnection.service.ClientNotificationService;
import net.study.messageconnection.service.UserConnectionService;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InviteRequestHandler implements BaseRequestHandler<InviteRequest> {

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, InviteRequest request) {
        UserId inviterUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        Pair<Optional<UserId>, String> result = userConnectionService.invite(inviterUserId, request.getUserInviteCode());

        result.getFirst()
                .ifPresentOrElse(partnerUserId -> {
                    String inviterUsername = result.getSecond();
                    clientNotificationService.sendError(senderSession, inviterUserId, new InviteResponse(request.getUserInviteCode(), UserConnectionStatus.PENDING));
                    clientNotificationService.sendMessage(partnerUserId, new InviteNotification(inviterUsername));
                }, () -> {
                    String errorMessage = result.getSecond();
                    clientNotificationService.sendError(senderSession, inviterUserId, new ErrorResponse(errorMessage, MessageType.INVITE_REQUEST));
                });
    }

    @Override
    public Class<InviteRequest> getRequestType() {
        return InviteRequest.class;
    }
}
