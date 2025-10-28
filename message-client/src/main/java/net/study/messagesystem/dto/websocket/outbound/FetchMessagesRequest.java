package net.study.messagesystem.dto.websocket.outbound;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.channel.ChannelId;
import net.study.messagesystem.dto.message.MessageSeqId;

@Getter
@EqualsAndHashCode(of = {"channelId", "startMessageSeqId", "endMessageSeqId"}, callSuper = false)
public class FetchMessagesRequest extends BaseRequest {

    private final ChannelId channelId;
    private final MessageSeqId startMessageSeqId;
    private final MessageSeqId endMessageSeqId;

    public FetchMessagesRequest(
            ChannelId channelId,
            MessageSeqId startMessageSeqId,
            MessageSeqId endMessageSeqId
    ) {
        super(MessageType.FETCH_MESSAGES_REQUEST);
        this.channelId = channelId;
        this.startMessageSeqId = startMessageSeqId;
        this.endMessageSeqId = endMessageSeqId;
    }
}
