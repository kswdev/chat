package net.study.messageconnection.handler.websocket;

import com.mysema.commons.lang.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.constant.ResultType;
import net.study.messageconnection.domain.channel.Channel;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.websocket.inbound.JoinRequest;
import net.study.messageconnection.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnection.dto.websocket.outbound.JoinResponse;
import net.study.messageconnection.service.ChannelService;
import net.study.messageconnection.service.ClientNotificationService;
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
            clientNotificationService.sendError(senderSession, senderUserId, new ErrorResponse(e.getMessage(), ResultType.FAILED.getMessage()));
            return;
        }

        result.getFirst()
              .ifPresentOrElse(channel ->
                      clientNotificationService.sendError(senderSession, senderUserId, new JoinResponse(channel.channelId(), channel.title())),
                      () -> clientNotificationService.sendError(senderSession, senderUserId, new ErrorResponse(result.getSecond().getMessage(), MessageType.JOIN_REQUEST)));
    }

    @Override
    public Class<JoinRequest> getRequestType() {
        return JoinRequest.class;
    }
}
