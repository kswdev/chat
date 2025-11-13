package net.study.messageconnection.domain.message;


import net.study.messageconnection.domain.channel.ChannelId;

public record Message(
        ChannelId channelId,
        MessageSeqId messageSeqId,
        String username, String content
) { }
