package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.channel.ChannelId;

@Getter
public class QuitResponse extends BaseMessage{

    private final ChannelId channelId;

    @JsonCreator
    public QuitResponse(
            @JsonProperty("channelId") ChannelId channelId
    ) {
        super(MessageType.QUIT_RESPONSE);
        this.channelId = channelId;
    }
}
