package net.study.messageconnection.domain.channel;


import net.study.messageconnection.domain.message.MessageSeqId;

public record ChannelEntry(String title, MessageSeqId lastReadMessageSeqId, MessageSeqId lastChannelMessageSeqId) {
}
