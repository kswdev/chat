package net.study.messagesystem.handler.websocket;

import com.mysema.commons.lang.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.IdKey;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.ResultType;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.message.Message;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.websocket.inbound.FetchMessagesRequest;
import net.study.messagesystem.dto.websocket.outbound.ErrorResponse;
import net.study.messagesystem.dto.websocket.outbound.FetchMessagesResponse;
import net.study.messagesystem.service.ClientNotificationService;
import net.study.messagesystem.service.MessageService;
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
            clientNotificationService.sendMessage(
                    senderSession, senderUserId, new FetchMessagesResponse(channelId, messages));
        } else {
            clientNotificationService.sendMessage(
                    senderSession, senderUserId, new ErrorResponse(result.getSecond().getMessage(), MessageType.FETCH_MESSAGES_REQUEST));
        }
    }

    @Override
    public Class<FetchMessagesRequest> getRequestType() {
        return FetchMessagesRequest.class;
    }
}
