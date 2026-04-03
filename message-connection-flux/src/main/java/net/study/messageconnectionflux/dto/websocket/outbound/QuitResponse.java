package net.study.messageconnectionflux.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.domain.channel.ChannelId;

@Getter
public class QuitResponse extends BaseMessage {

    private final ChannelId channelId;

    public QuitResponse(ChannelId channelId) {
        super(MessageType.QUIT_RESPONSE);
        this.channelId = channelId;
    }
}
