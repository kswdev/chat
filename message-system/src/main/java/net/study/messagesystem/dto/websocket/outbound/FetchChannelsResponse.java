package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.domain.channel.Channel;

import java.util.List;

@Getter
public class FetchChannelsResponse extends BaseMessage{

    private final List<Channel> channels;

    public FetchChannelsResponse(List<Channel> channels) {
        super(MessageType.FETCH_CHANNELS_RESPONSE);
        this.channels = channels;
    }
}
