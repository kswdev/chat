package net.study.messagesystem.handler.websocket;

import com.mysema.commons.lang.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.IdKey;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.ResultType;
import net.study.messagesystem.dto.domain.channel.Channel;
import net.study.messagesystem.dto.domain.user.UserId;
import net.study.messagesystem.dto.websocket.inbound.JoinRequest;
import net.study.messagesystem.dto.websocket.outbound.ErrorResponse;
import net.study.messagesystem.dto.websocket.outbound.JoinResponse;
import net.study.messagesystem.service.ChannelService;
import net.study.messagesystem.service.ClientNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JoinRequestHandler implements BaseRequestHandler<JoinRequest> {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, JoinRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        Pair<Optional<Channel>, ResultType> result;

        try {
            result = channelService.join(request.getInviteCode(), senderUserId);
        } catch (Exception e) {
            clientNotificationService.sendMessage(senderSession, senderUserId, new ErrorResponse(e.getMessage(), ResultType.FAILED.getMessage()));
            return;
        }

        result.getFirst()
              .ifPresentOrElse(channel ->
                      clientNotificationService.sendMessage(senderSession, senderUserId, new JoinResponse(channel.channelId(), channel.title())),
                      () -> clientNotificationService.sendMessage(senderSession, senderUserId, new ErrorResponse(result.getSecond().getMessage(), MessageType.JOIN_REQUEST)));
    }

    @Override
    public Class<JoinRequest> getRequestType() {
        return JoinRequest.class;
    }
}
