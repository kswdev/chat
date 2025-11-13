package net.study.messageconnection.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.channel.ChannelId;

@Getter
public class WriteMessage extends BaseRequest {

    private final ChannelId channelId;
    private final String content;
    private final Long serial;

    @JsonCreator
    public WriteMessage(
            @JsonProperty("channelId") ChannelId channelId,
            @JsonProperty("content") String content,
            @JsonProperty("serial") Long serial
    ) {
        super(MessageType.WRITE_MESSAGE);
        this.channelId = channelId;
        this.content = content;
        this.serial = serial;
    }
}
