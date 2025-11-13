package net.study.messageconnection.handler.websocket;

import com.mysema.commons.lang.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.constant.ResultType;
import net.study.messageconnection.domain.channel.ChannelEntry;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.websocket.inbound.EnterRequest;
import net.study.messageconnection.dto.websocket.outbound.EnterResponse;
import net.study.messageconnection.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnection.service.ChannelService;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnterRequestHandler implements BaseRequestHandler<EnterRequest> {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, EnterRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        Pair<Optional<ChannelEntry>, ResultType> result = channelService.enter(request.getChannelId(), senderUserId);

        result.getFirst()
              .ifPresentOrElse(entry ->
                    clientNotificationService.sendError(senderSession, senderUserId, new EnterResponse(request.getChannelId(), entry.title(), entry.lastChannelMessageSeqId(), entry.lastReadMessageSeqId()))
                    ,() -> clientNotificationService.sendError(senderSession, senderUserId, new ErrorResponse(result.getSecond().getMessage(), MessageType.ENTER_REQUEST))
              );
    }

    @Override
    public Class<EnterRequest> getRequestType() {
        return EnterRequest.class;
    }
}
