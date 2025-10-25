package net.study.messagesystem.domain.channel;

import net.study.messagesystem.domain.message.MessageSeqId;

public record ChannelEntry(String title, MessageSeqId lastReadMessageSeqId, MessageSeqId lastChannelMessageSeqId) {
}
