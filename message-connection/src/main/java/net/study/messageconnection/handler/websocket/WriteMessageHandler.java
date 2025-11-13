package net.study.messageconnection.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.domain.channel.ChannelId;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.websocket.inbound.WriteMessage;
import net.study.messageconnection.dto.websocket.outbound.MessageNotification;
import net.study.messageconnection.handler.websocket.BaseRequestHandler;
import net.study.messageconnection.service.MessageSeqIdGenerator;
import net.study.messageconnection.service.MessageService;
import net.study.messageconnection.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class WriteMessageHandler implements BaseRequestHandler<WriteMessage> {

    private final UserService userService;
    private final MessageService messageService;
    private final MessageSeqIdGenerator sequenceGenerator;

    @Override
    public void handleRequest(WebSocketSession senderSession, WriteMessage request) {
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
                                request.getSerial(),
                                new MessageNotification(channelId, messageSeqId, senderUsername, content)));
    }

    @Override
    public Class<WriteMessage> getRequestType() {
        return WriteMessage.class;
    }
}
