package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.channel.ChannelId;

@Getter
public class EnterRequest extends BaseRequest {

    private final ChannelId channelId;

    @JsonCreator
    public EnterRequest(
            @JsonProperty("channelId") ChannelId ChannelId
    ) {
        super(MessageType.ENTER_REQUEST);
        this.channelId = ChannelId;
    }
}
