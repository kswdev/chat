package net.study.messageconnection.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.websocket.inbound.FetchChannelInviteCodeRequest;
import net.study.messageconnection.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnection.dto.websocket.outbound.FetchChannelInviteCodeResponse;
import net.study.messageconnection.service.ChannelService;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchChannelInviteCodeRequestHandler implements BaseRequestHandler<FetchChannelInviteCodeRequest> {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, FetchChannelInviteCodeRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        boolean isJoined = channelService.isJoined(senderUserId, request.getChannelId());

        if (!isJoined) {
            clientNotificationService.sendError(senderSession, senderUserId, new ErrorResponse("Not joined channel.", MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST));
            return;
        }

        channelService
                .getInviteCode(request.getChannelId())
                .ifPresentOrElse(inviteCode ->
                      clientNotificationService.sendError(senderSession, senderUserId, new FetchChannelInviteCodeResponse(request.getChannelId(), inviteCode)),
                () ->
                      clientNotificationService.sendError(senderSession, senderUserId, new ErrorResponse("Fetch channel invite code failed.", MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST)));

    }

    @Override
    public Class<FetchChannelInviteCodeRequest> getRequestType() {
        return FetchChannelInviteCodeRequest.class;
    }
}
