package net.study.messageconnection.handler.websocket;

import com.mysema.commons.lang.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.constant.ResultType;
import net.study.messageconnection.domain.channel.ChannelId;
import net.study.messageconnection.domain.message.Message;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.websocket.inbound.FetchMessagesRequest;
import net.study.messageconnection.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnection.dto.websocket.outbound.FetchMessagesResponse;
import net.study.messageconnection.service.ClientNotificationService;
import net.study.messageconnection.service.MessageService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchMessageRequestHandler implements BaseRequestHandler<FetchMessagesRequest> {

    private final MessageService messageService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, FetchMessagesRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        ChannelId channelId = request.getChannelId();

        Pair<List<Message>, ResultType> result = messageService.getMessages(channelId, request.getStartMessageSeqId(), request.getEndMessageSeqId());

        if (result.getSecond().equals(ResultType.SUCCESS)) {
            List<Message> messages = result.getFirst();
            clientNotificationService.sendError(
                    senderSession, senderUserId, new FetchMessagesResponse(channelId, messages));
        } else {
            clientNotificationService.sendError(
                    senderSession, senderUserId, new ErrorResponse(result.getSecond().getMessage(), MessageType.FETCH_MESSAGES_REQUEST));
        }
    }

    @Override
    public Class<FetchMessagesRequest> getRequestType() {
        return FetchMessagesRequest.class;
    }
}
