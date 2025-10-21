package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.channel.ChannelId;

@Getter
public class MessageNotification extends BaseMessage{

    private final ChannelId channelId;
    private final String username;
    private final String content;

    public MessageNotification(ChannelId channelId, String username, String content) {
        super(MessageType.NOTIFY_MESSAGE);
        this.channelId = channelId;
        this.username = username;
        this.content = content;
    }
}
