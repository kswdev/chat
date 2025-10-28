package net.study.messagesystem.dto.websocket.outbound;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.channel.ChannelId;
import net.study.messagesystem.dto.message.MessageSeqId;

@Getter
@EqualsAndHashCode(of = {"messageSeqId", "channelId"}, callSuper = false)
public class ReadMessageAck extends BaseRequest {

    private final ChannelId channelId;
    private final MessageSeqId messageSeqId;

    public ReadMessageAck(
            ChannelId channelId,
            MessageSeqId messageSeqId
    ) {
        super(MessageType.READ_MESSAGE_ACK);
        this.channelId = channelId;
        this.messageSeqId = messageSeqId;
    }
}
