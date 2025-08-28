package net.study.messagesystem.handler.websocket;

import com.mysema.commons.lang.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.IdKey;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.ResultType;
import net.study.messagesystem.dto.domain.channel.ChannelId;
import net.study.messagesystem.dto.domain.user.UserId;
import net.study.messagesystem.dto.websocket.inbound.EnterRequest;
import net.study.messagesystem.dto.websocket.outbound.EnterResponse;
import net.study.messagesystem.dto.websocket.outbound.ErrorResponse;
import net.study.messagesystem.service.ChannelService;
import net.study.messagesystem.session.WebSocketSessionManager;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.awt.*;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnterRequestHandler implements BaseRequestHandler<EnterRequest> {

    private final ChannelService channelService;
    private final WebSocketSessionManager webSocketSessionManager;

    @Override
    public void handleRequest(WebSocketSession senderSession, EnterRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        Pair<Optional<String>, ResultType> result = channelService.enter(request.getChannelId(), senderUserId);

        result.getFirst()
                .ifPresentOrElse(title ->
                        webSocketSessionManager.sendMessage(senderSession, new EnterResponse(request.getChannelId(), title))
                        ,() -> webSocketSessionManager.sendMessage(senderSession, new ErrorResponse(result.getSecond().getMessage(), MessageType.ENTER_REQUEST))
                );
    }

    @Override
    public Class<EnterRequest> getRequestType() {
        return EnterRequest.class;
    }
}
