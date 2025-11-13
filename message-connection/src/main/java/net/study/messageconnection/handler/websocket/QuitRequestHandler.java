package net.study.messageconnection.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.constant.ResultType;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.websocket.inbound.QuitRequest;
import net.study.messageconnection.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnection.dto.websocket.outbound.QuitResponse;
import net.study.messageconnection.service.ChannelService;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuitRequestHandler implements BaseRequestHandler<QuitRequest> {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, QuitRequest request) {
        UserId userId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        ResultType result;
        try {
            result = channelService.quit(request.getChannelId(), userId);
        } catch (Exception e) {
            clientNotificationService.sendError(senderSession, userId, new ErrorResponse(ResultType.FAILED.getMessage(), MessageType.QUIT_REQUEST));
            return;
        }

        if (result.equals(ResultType.SUCCESS))
            clientNotificationService.sendError(senderSession, userId, new QuitResponse(request.getChannelId()));
        else
            clientNotificationService.sendError(senderSession, userId, new ErrorResponse(result.getMessage(), MessageType.QUIT_REQUEST));
    }

    @Override
    public Class<QuitRequest> getRequestType() {
        return QuitRequest.class;
    }
}
