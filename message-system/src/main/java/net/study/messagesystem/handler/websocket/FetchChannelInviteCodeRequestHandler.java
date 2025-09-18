package net.study.messagesystem.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.IdKey;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.domain.user.UserId;
import net.study.messagesystem.dto.websocket.inbound.FetchChannelInviteCodeRequest;
import net.study.messagesystem.dto.websocket.outbound.ErrorResponse;
import net.study.messagesystem.dto.websocket.outbound.FetchChannelInviteCodeResponse;
import net.study.messagesystem.service.ChannelService;
import net.study.messagesystem.service.ClientNotificationService;
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
            clientNotificationService.sendMessage(senderSession, senderUserId, new ErrorResponse("Not joined channel.", MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST));
            return;
        }

        channelService
                .getInviteCode(request.getChannelId())
                .ifPresentOrElse(inviteCode ->
                      clientNotificationService.sendMessage(senderSession, senderUserId, new FetchChannelInviteCodeResponse(request.getChannelId(), inviteCode)),
                () ->
                      clientNotificationService.sendMessage(senderSession, senderUserId, new ErrorResponse("Fetch channel invite code failed.", MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST)));

    }

    @Override
    public Class<FetchChannelInviteCodeRequest> getRequestType() {
        return FetchChannelInviteCodeRequest.class;
    }
}
