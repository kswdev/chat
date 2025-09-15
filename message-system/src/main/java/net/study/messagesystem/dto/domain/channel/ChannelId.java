package net.study.messagesystem.dto.domain.channel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record ChannelId(
        @JsonValue Long id
) {

    @JsonCreator
    public ChannelId {
        if (id == null || id < 0)
            throw new IllegalArgumentException("Invalid ChannelId");
    }
}
