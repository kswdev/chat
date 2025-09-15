package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.channel.ChannelId;

@Getter
public class EnterRequest extends BaseRequest {

    private final ChannelId channelId;

    public EnterRequest(
            ChannelId ChannelId
    ) {
        super(MessageType.ENTER_REQUEST);
        this.channelId = ChannelId;
    }
}
