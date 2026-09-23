package net.study.messagesystem.dto.rest.channel;

import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.message.MessageSeqId;

public record ChannelEntryResponse(
        ChannelId channelId,
        String title,
        MessageSeqId lastReadMessageSeqId,
        MessageSeqId lastChannelMessageSeqId
) { }
