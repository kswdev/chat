package net.study.messagesystem.dto.message;

import com.fasterxml.jackson.annotation.JsonValue;
import net.study.messagesystem.dto.channel.ChannelId;

public record Message (
        @JsonValue ChannelId channelId,
        @JsonValue MessageSeqId messageSeqId,
        @JsonValue String username,
        @JsonValue String content
) implements Comparable<Message> {

    @Override
    public int compareTo(Message o) {
        return Long.compare(messageSeqId.id(), o.messageSeqId.id());
    }
}
