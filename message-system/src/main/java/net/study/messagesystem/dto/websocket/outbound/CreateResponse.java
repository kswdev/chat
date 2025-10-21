package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.channel.ChannelId;

@Getter
public class CreateResponse extends BaseMessage{

    private final ChannelId channelId;
    private final String title;

    public CreateResponse(ChannelId channelId, String title) {
        super(MessageType.CREATE_RESPONSE);
        this.channelId = channelId;
        this.title = title;
    }
}
