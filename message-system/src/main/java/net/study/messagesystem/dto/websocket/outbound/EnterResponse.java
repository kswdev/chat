package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.message.MessageSeqId;

@Getter
public class EnterResponse extends BaseMessage{

    private final ChannelId channelId;
    private final MessageSeqId lastReadMessageSeqId;
    private final MessageSeqId lastChannelMessageSeqId;
    private final String title;

    public EnterResponse(
            ChannelId channelId,
            String title,
            MessageSeqId lastChannelMessageSeqId,
            MessageSeqId lastReadMessageSeqId
            ) {
        super(MessageType.ENTER_RESPONSE);
        this.channelId = channelId;
        this.title = title;
        this.lastChannelMessageSeqId = lastChannelMessageSeqId;
        this.lastReadMessageSeqId = lastReadMessageSeqId;
    }
}
