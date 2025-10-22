package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.message.MessageSeqId;

@Getter
public class ReadMessageAck extends BaseRequest {

    private final ChannelId channelId;
    private final MessageSeqId messageSeqId;

    @JsonCreator
    public ReadMessageAck(
            @JsonProperty("channelId") ChannelId channelId,
            @JsonProperty("messageSeqId") MessageSeqId messageSeqId
    ) {
        super(MessageType.WRITE_MESSAGE);
        this.channelId = channelId;
        this.messageSeqId = messageSeqId;
    }
}
