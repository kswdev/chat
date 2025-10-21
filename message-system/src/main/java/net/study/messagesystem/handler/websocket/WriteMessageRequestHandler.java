package net.study.messagesystem.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.IdKey;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.message.MessageSeqId;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.websocket.inbound.WriteMessageRequest;
import net.study.messagesystem.dto.websocket.outbound.MessageNotification;
import net.study.messagesystem.service.MessageSeqIdGenerator;
import net.study.messagesystem.service.MessageService;
import net.study.messagesystem.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class WriteMessageRequestHandler implements BaseRequestHandler<WriteMessageRequest> {

    private final UserService userService;
    private final MessageService messageService;
    private final MessageSeqIdGenerator sequenceGenerator;

    @Override
    public void handleRequest(WebSocketSession senderSession, WriteMessageRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        ChannelId channelId = request.getChannelId();
        String content = request.getContent();
        String senderUsername = userService.getUsername(senderUserId).orElse("unknown");

        sequenceGenerator
                .getNext(channelId)
                .ifPresent(messageSeqId ->
                        messageService.sendMessage(
                                senderUserId,
                                content,
                                channelId,
                                messageSeqId,
                                new MessageNotification(channelId, messageSeqId, senderUsername, content)));
    }

    @Override
    public Class<WriteMessageRequest> getRequestType() {
        return WriteMessageRequest.class;
    }
}
