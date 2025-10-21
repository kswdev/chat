package net.study.messagesystem.domain.message;

import net.study.messagesystem.domain.channel.ChannelId;

public record Message(
        ChannelId channelId,
        MessageSeqId messageSeqId,
        String username, String content
) { }
