package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.channel.ChannelId;

@Getter
public class MessageNotification extends BaseMessage{

    private final ChannelId channelId;
    private final String username;
    private final String content;

    @JsonCreator
    public MessageNotification(
            @JsonProperty("channelId") ChannelId channelId,
            @JsonProperty("username") String username,
            @JsonProperty("content") String content
    ) {
        super(MessageType.NOTIFY_MESSAGE);
        this.channelId = channelId;
        this.username = username;
        this.content = content;
    }
}
