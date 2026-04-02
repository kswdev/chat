package net.study.messageconnection.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagecommon.constant.MessageType;
import net.study.messageconnection.domain.channel.Channel;

import java.util.List;

@Getter
public class FetchChannelsResponse extends BaseMessage {

    private final List<Channel> channels;

    public FetchChannelsResponse(List<Channel> channels) {
        super(MessageType.FETCH_CHANNELS_RESPONSE);
        this.channels = channels;
    }
}
