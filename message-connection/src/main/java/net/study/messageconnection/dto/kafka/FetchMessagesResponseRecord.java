package net.study.messageconnection.dto.kafka;

import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.channel.ChannelId;
import net.study.messageconnection.domain.message.MessageSeqId;
import net.study.messageconnection.domain.user.UserId;

public record FetchMessagesResponseRecord(
        UserId userId,
        ChannelId channelId,
        MessageSeqId startMessageSeqId,
        MessageSeqId endMessageSeqId
) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_MESSAGES_RESPONSE;
    }
}
