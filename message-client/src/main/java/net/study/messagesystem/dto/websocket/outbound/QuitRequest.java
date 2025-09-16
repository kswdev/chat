package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.channel.ChannelId;

@Getter
public class QuitRequest extends BaseRequest {

    private final ChannelId channelId;

    public QuitRequest(ChannelId ChannelId) {
        super(MessageType.QUIT_REQUEST);
        this.channelId = ChannelId;
    }
}
