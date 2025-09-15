package net.study.messagesystem.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.IdKey;
import net.study.messagesystem.dto.domain.channel.Channel;
import net.study.messagesystem.dto.domain.user.UserId;
import net.study.messagesystem.dto.websocket.inbound.FetchChannelsRequest;
import net.study.messagesystem.dto.websocket.outbound.FetchChannelsResponse;
import net.study.messagesystem.service.ChannelService;
import net.study.messagesystem.session.WebSocketSessionManager;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchChannelsRequestHandler implements BaseRequestHandler<FetchChannelsRequest> {

    private final ChannelService channelService;
    private final WebSocketSessionManager webSocketSessionManager;

    @Override
    public void handleRequest(WebSocketSession senderSession, FetchChannelsRequest request) {
        UserId requestUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        webSocketSessionManager.sendMessage(
                senderSession, new FetchChannelsResponse(channelService.getChannels(requestUserId)));
    }
    @Override
    public Class<FetchChannelsRequest> getRequestType() {
        return FetchChannelsRequest.class;
    }
}
