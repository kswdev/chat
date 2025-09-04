package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.channel.ChannelId;

@Getter
public class JoinNotification extends BaseMessage{

    private final ChannelId channelId;
    private final String title;

    @JsonCreator
    public JoinNotification(
            @JsonProperty("channelId") ChannelId channelId,
            @JsonProperty("title") String title
    ) {
        super(MessageType.NOTIFY_JOIN);
        this.channelId = channelId;
        this.title = title;
    }
}
