package net.study.messageconnectionflux.application.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.domain.channel.ChannelId;
import net.study.messageconnectionflux.domain.message.MessageSeqId;
import net.study.messageconnectionflux.domain.user.UserId;

public record FetchMessagesRequestRecord(
        UserId userId, ChannelId channelId,
        MessageSeqId startMessageSeqId,
        MessageSeqId endMessageSeqId
) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_MESSAGES_REQUEST;
    }
}
