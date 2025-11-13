package net.study.messageconnection.dto.kafka;

import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.channel.ChannelId;
import net.study.messageconnection.domain.message.MessageSeqId;
import net.study.messageconnection.domain.user.UserId;


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