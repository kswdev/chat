package net.study.messageconnectionflux.application.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.domain.channel.ChannelId;
import net.study.messageconnectionflux.domain.message.MessageSeqId;
import net.study.messageconnectionflux.domain.user.UserId;

public record ReadMessageAckRecord(
        UserId userId,
        ChannelId channelId,
        MessageSeqId messageSeqId
) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.READ_MESSAGE_ACK;
    }
}
