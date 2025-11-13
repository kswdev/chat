package net.study.messageconnection.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.channel.ChannelId;
import net.study.messageconnection.domain.message.MessageSeqId;

@Getter
public class ReadMessageAck extends BaseRequest {

    private final ChannelId channelId;
    private final MessageSeqId messageSeqId;

    @JsonCreator
    public ReadMessageAck(
            @JsonProperty("channelId") ChannelId channelId,
            @JsonProperty("messageSeqId") MessageSeqId messageSeqId
    ) {
        super(MessageType.READ_MESSAGE_ACK);
        this.channelId = channelId;
        this.messageSeqId = messageSeqId;
    }
}
