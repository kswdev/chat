package net.study.messagesystem.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.IdKey;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.ResultType;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.websocket.inbound.QuitRequest;
import net.study.messagesystem.dto.websocket.outbound.ErrorResponse;
import net.study.messagesystem.dto.websocket.outbound.QuitResponse;
import net.study.messagesystem.service.ChannelService;
import net.study.messagesystem.service.ClientNotificationService;
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
            clientNotificationService.sendMessage(senderSession, userId, new ErrorResponse(ResultType.FAILED.getMessage(), MessageType.QUIT_REQUEST));
            return;
        }

        if (result.equals(ResultType.SUCCESS))
            clientNotificationService.sendMessage(senderSession, userId, new QuitResponse(request.getChannelId()));
        else
            clientNotificationService.sendMessage(senderSession, userId, new ErrorResponse(result.getMessage(), MessageType.QUIT_REQUEST));
    }

    @Override
    public Class<QuitRequest> getRequestType() {
        return QuitRequest.class;
    }
}
