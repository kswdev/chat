package net.study.messageconnection.dto.websocket.outbound;

import lombok.Getter;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.channel.ChannelId;
import net.study.messageconnection.domain.message.MessageSeqId;

@Getter
public class MessageNotification extends BaseMessage {

    private final ChannelId channelId;
    private final MessageSeqId messageSeqId;
    private final String username;
    private final String content;

    public MessageNotification(ChannelId channelId, MessageSeqId messageSeqId, String username, String content) {
        super(MessageType.NOTIFY_MESSAGE);
        this.channelId = channelId;
        this.messageSeqId = messageSeqId;
        this.username = username;
        this.content = content;
    }
}
