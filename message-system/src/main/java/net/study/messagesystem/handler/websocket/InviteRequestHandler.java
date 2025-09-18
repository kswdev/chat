package net.study.messagesystem.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.IdKey;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.dto.domain.user.UserId;
import net.study.messagesystem.dto.websocket.inbound.InviteRequest;
import net.study.messagesystem.dto.websocket.outbound.ErrorResponse;
import net.study.messagesystem.dto.websocket.outbound.InviteNotification;
import net.study.messagesystem.dto.websocket.outbound.InviteResponse;
import net.study.messagesystem.service.ClientNotificationService;
import net.study.messagesystem.service.UserConnectionService;
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
                    clientNotificationService.sendMessage(senderSession, inviterUserId, new InviteResponse(request.getUserInviteCode(), UserConnectionStatus.PENDING));
                    clientNotificationService.sendMessage(partnerUserId, new InviteNotification(inviterUsername));
                }, () -> {
                    String errorMessage = result.getSecond();
                    clientNotificationService.sendMessage(senderSession, inviterUserId, new ErrorResponse(errorMessage, MessageType.INVITE_REQUEST));
                });
    }

    @Override
    public Class<InviteRequest> getRequestType() {
        return InviteRequest.class;
    }
}
