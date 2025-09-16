package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.channel.Channel;

import java.util.List;

@Getter
public class FetchChannelsResponse extends BaseMessage{

    private final List<Channel> channels;

    @JsonCreator
    public FetchChannelsResponse(
            @JsonProperty("channels") List<Channel> channels
    ) {
        super(MessageType.FETCH_CHANNELS_RESPONSE);
        this.channels = channels;
    }
}
