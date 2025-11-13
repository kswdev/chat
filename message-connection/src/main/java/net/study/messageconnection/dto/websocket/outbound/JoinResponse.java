package net.study.messageconnection.dto.websocket.outbound;

import lombok.Getter;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.channel.ChannelId;

@Getter
public class JoinResponse extends BaseMessage {

    private final ChannelId channelId;
    private final String title;

    public JoinResponse(ChannelId channelId, String title) {
        super(MessageType.JOIN_RESPONSE);
        this.channelId = channelId;
        this.title = title;
    }
}
