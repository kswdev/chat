package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.channel.ChannelId;

@Getter
public class WriteMessage extends BaseRequest {

    private final ChannelId channelId;
    private final String content;

    @JsonCreator
    public WriteMessage(
            @JsonProperty("channelId") ChannelId channelId,
            @JsonProperty("content") String content
    ) {
        super(MessageType.WRITE_MESSAGE);
        this.channelId = channelId;
        this.content = content;
    }
}
