package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.domain.channel.ChannelId;

@Getter
public class EnterResponse extends BaseMessage{

    private final ChannelId channelId;
    private final String title;

    public EnterResponse(ChannelId channelId, String title) {
        super(MessageType.ENTER_RESPONSE);
        this.channelId = channelId;
        this.title = title;
    }
}
