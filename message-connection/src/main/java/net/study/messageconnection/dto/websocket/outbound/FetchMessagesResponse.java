package net.study.messageconnection.dto.websocket.outbound;

import lombok.Getter;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.channel.ChannelId;
import net.study.messageconnection.domain.message.Message;

import java.util.List;

@Getter
public class FetchMessagesResponse extends BaseMessage {

    private final ChannelId channelId;
    private final List<Message> messages;

    public FetchMessagesResponse(ChannelId channelId, List<Message> messages) {
        super(MessageType.FETCH_MESSAGES_RESPONSE);
        this.channelId = channelId;
        this.messages = messages;
    }
}
