package net.study.messageconnectionflux.application.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.domain.channel.ChannelId;
import net.study.messageconnectionflux.domain.message.MessageSeqId;

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
