package net.study.messageconnection.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.websocket.inbound.FetchChannelsRequest;
import net.study.messageconnection.dto.websocket.outbound.FetchChannelsResponse;
import net.study.messageconnection.service.ChannelService;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchChannelsRequestHandler implements BaseRequestHandler<FetchChannelsRequest> {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, FetchChannelsRequest request) {
        UserId requestUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        clientNotificationService.sendError(
                senderSession, requestUserId, new FetchChannelsResponse(channelService.getChannels(requestUserId)));
    }
    @Override
    public Class<FetchChannelsRequest> getRequestType() {
        return FetchChannelsRequest.class;
    }
}
