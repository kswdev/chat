package net.study.messageconnection.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.channel.ChannelId;
import net.study.messageconnection.domain.message.MessageSeqId;

@Getter
public class FetchMessagesRequest extends BaseRequest {

    private final ChannelId channelId;
    private final MessageSeqId startMessageSeqId;
    private final MessageSeqId endMessageSeqId;

    @JsonCreator
    public FetchMessagesRequest(
            @JsonProperty("channelId") ChannelId channelId,
            @JsonProperty("startMessageSeqId") MessageSeqId startMessageSeqId,
            @JsonProperty("endMessageSeqId") MessageSeqId endMessageSeqId
    ) {
        super(MessageType.FETCH_MESSAGES_REQUEST);
        this.channelId = channelId;
        this.startMessageSeqId = startMessageSeqId;
        this.endMessageSeqId = endMessageSeqId;
    }
}
