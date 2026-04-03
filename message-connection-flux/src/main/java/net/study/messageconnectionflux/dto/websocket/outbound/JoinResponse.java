package net.study.messageconnectionflux.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.domain.channel.ChannelId;

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
