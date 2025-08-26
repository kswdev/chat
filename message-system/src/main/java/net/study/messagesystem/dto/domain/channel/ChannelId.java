package net.study.messagesystem.dto.domain.channel;

public record ChannelId(Long id) {

    public ChannelId {
        if (id == null || id < 0)
            throw new IllegalArgumentException("Invalid ChannelId");
    }
}
