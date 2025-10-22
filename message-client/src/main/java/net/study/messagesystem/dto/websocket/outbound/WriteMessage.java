package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.channel.ChannelId;

@Getter
public class WriteMessage extends BaseRequest {

    private final ChannelId channelId;
    private final String username;
    private final String content;

    public WriteMessage(ChannelId channelId, String username, String content) {
        super(MessageType.WRITE_MESSAGE);
        this.channelId = channelId;
        this.username = username;
        this.content = content;
    }
}
