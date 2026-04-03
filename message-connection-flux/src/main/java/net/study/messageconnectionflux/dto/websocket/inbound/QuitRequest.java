package net.study.messageconnectionflux.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.domain.channel.ChannelId;

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
