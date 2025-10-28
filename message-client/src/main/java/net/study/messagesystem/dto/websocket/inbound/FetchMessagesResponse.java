package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.channel.ChannelId;
import net.study.messagesystem.dto.message.Message;

import java.util.List;

@Getter
public class FetchMessagesResponse extends BaseMessage{

    private final ChannelId channelId;
    private final List<Message> messages;

    @JsonCreator
    public FetchMessagesResponse(
            @JsonProperty("channelId") ChannelId channelId,
            @JsonProperty("messages") List<Message> messages
    ) {
        super(MessageType.FETCH_MESSAGES_RESPONSE);
        this.channelId = channelId;
        this.messages = messages;
    }
}
