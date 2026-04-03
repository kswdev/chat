package net.study.messageconnectionflux.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.domain.channel.ChannelId;
import net.study.messageconnectionflux.domain.message.MessageSeqId;
import net.study.messageconnectionflux.domain.user.UserId;

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
