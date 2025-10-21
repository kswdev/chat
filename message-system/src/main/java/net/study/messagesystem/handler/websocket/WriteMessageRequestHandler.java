package net.study.messagesystem.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.IdKey;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.websocket.inbound.WriteMessageRequest;
import net.study.messagesystem.dto.websocket.outbound.MessageNotification;
import net.study.messagesystem.service.MessageService;
import net.study.messagesystem.service.UserService;
import net.study.messagesystem.service.ClientNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class WriteMessageRequestHandler implements BaseRequestHandler<WriteMessageRequest> {

    private final UserService userService;
    private final MessageService messageService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, WriteMessageRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        String content = request.getContent();
        ChannelId channelId = request.getChannelId();
        String senderUsername = userService.getUsername(senderUserId).orElse("unknown");

        messageService.sendMessage(
                senderUserId,
                content,
                channelId,
                new MessageNotification(channelId, senderUsername, content));
    }

    @Override
    public Class<WriteMessageRequest> getRequestType() {
        return WriteMessageRequest.class;
    }
}
