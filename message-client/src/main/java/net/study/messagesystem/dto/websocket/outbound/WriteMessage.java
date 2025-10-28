package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.channel.ChannelId;

@Getter
public class WriteMessage extends BaseRequest {

    private final ChannelId channelId;
    private final String content;
    private final Long serial;

    public WriteMessage(ChannelId channelId, String content, Long serial) {
        super(MessageType.WRITE_MESSAGE);
        this.channelId = channelId;
        this.content = content;
        this.serial = serial;
    }
}
