package net.study.messagesystem.dto.kafka;

import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.message.MessageSeqId;
import net.study.messagesystem.domain.user.UserId;


public record EnterResponseRecord(
        UserId userId,
        ChannelId channelId,
        String title,
        MessageSeqId lastReadMessageSeqId,
        MessageSeqId lastChannelMessageSeqId
) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.ENTER_RESPONSE;
    }
}