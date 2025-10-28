package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.channel.ChannelId;
import net.study.messagesystem.dto.message.MessageSeqId;

@Getter
public class EnterResponse extends BaseMessage{

    private final ChannelId channelId;
    private final String title;
    private final MessageSeqId lastReadMessageSeqId;
    private final MessageSeqId lastChannelMessageSeqId;

    @JsonCreator
    public EnterResponse(
            @JsonProperty("channelId") ChannelId channelId,
            @JsonProperty("title") String title,
            @JsonProperty("lastReadMessageSeqId") MessageSeqId lastReadMessageSeqId,
            @JsonProperty("lastChannelMessageSeqId") MessageSeqId lastChannelMessageSeqId
    ) {
        super(MessageType.ENTER_RESPONSE);
        this.channelId = channelId;
        this.title = title;
        this.lastReadMessageSeqId = lastReadMessageSeqId;
        this.lastChannelMessageSeqId = lastChannelMessageSeqId;
    }
}
