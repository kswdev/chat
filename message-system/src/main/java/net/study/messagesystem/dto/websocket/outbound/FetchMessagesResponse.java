package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.message.Message;
import net.study.messagesystem.domain.message.MessageSeqId;

import java.util.List;

@Getter
public class FetchMessagesResponse extends BaseMessage{

    private final ChannelId channelId;
    private final List<Message> messages;

    public FetchMessagesResponse(ChannelId channelId, List<Message> messages) {
        super(MessageType.FETCH_MESSAGES_RESPONSE);
        this.channelId = channelId;
        this.messages = messages;
    }
}
