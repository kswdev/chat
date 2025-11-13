package net.study.messageconnection.dto.websocket.outbound;

import lombok.Getter;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.channel.ChannelId;

@Getter
public class JoinNotification extends BaseMessage {

    private final ChannelId channelId;
    private final String title;

    public JoinNotification(ChannelId channelId, String title) {
        super(MessageType.NOTIFY_JOIN);
        this.channelId = channelId;
        this.title = title;
    }
}
