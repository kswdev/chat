package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.domain.channel.ChannelId;

@Getter
public class LeaveRequest extends BaseRequest {

    @JsonProperty("channelId")
    private final ChannelId channelId;

    @JsonCreator
    public LeaveRequest(
            @JsonProperty("channelId") ChannelId channelId
    ) {
        super(MessageType.JOIN_REQUEST);
        this.channelId = channelId;
    }
}
