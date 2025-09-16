package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.domain.channel.ChannelId;

@Getter
public class QuitRequest extends BaseRequest {

    private final ChannelId channelId;

    @JsonCreator
    public QuitRequest(
            @JsonProperty("channelId") ChannelId ChannelId
    ) {
        super(MessageType.QUIT_REQUEST);
        this.channelId = ChannelId;
    }
}
