package net.study.messagesystem.dto.message;

import net.study.messagesystem.dto.channel.ChannelId;

public record Message(
        ChannelId channelId,
        MessageSeqId messageSeqId,
        String username, String content
) { }
