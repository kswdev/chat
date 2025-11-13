package net.study.messageconnection.dto.kafka;

import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.channel.ChannelId;
import net.study.messageconnection.domain.message.MessageSeqId;
import net.study.messageconnection.domain.user.UserId;

public record WriteMessageRecord(
        UserId userId,
        ChannelId channelId,
        String content,
        Long serial,
        MessageSeqId messageSeqId
) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.WRITE_MESSAGE;
    }
}
