package net.study.messagesystem.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.IdKey;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.websocket.inbound.ReadMessageAck;
import net.study.messagesystem.service.MessageService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReadMessageAckHandler implements BaseRequestHandler<ReadMessageAck> {

    private final MessageService messageService;

    @Override
    public void handleRequest(WebSocketSession senderSession, ReadMessageAck request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        ChannelId channelId = request.getChannelId();
        messageService.updateLastReadMsgSeq(senderUserId, channelId, request.getMessageSeqId());
    }

    @Override
    public Class<ReadMessageAck> getRequestType() {
        return ReadMessageAck.class;
    }
}
