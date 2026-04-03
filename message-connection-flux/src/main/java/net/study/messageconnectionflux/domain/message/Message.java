package net.study.messageconnectionflux.domain.message;


import net.study.messageconnectionflux.domain.channel.ChannelId;

public record Message(
        ChannelId channelId,
        MessageSeqId messageSeqId,
        String username, String content
) { }
