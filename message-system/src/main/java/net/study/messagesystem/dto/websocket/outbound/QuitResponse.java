package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.channel.ChannelId;

@Getter
public class QuitResponse extends BaseMessage{

    private final ChannelId channelId;

    public QuitResponse(ChannelId channelId) {
        super(MessageType.QUIT_RESPONSE);
        this.channelId = channelId;
    }
}
