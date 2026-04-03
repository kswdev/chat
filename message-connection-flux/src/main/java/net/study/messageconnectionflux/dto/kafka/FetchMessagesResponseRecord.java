package net.study.messageconnectionflux.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.domain.channel.ChannelId;
import net.study.messageconnectionflux.domain.message.Message;
import net.study.messageconnectionflux.domain.user.UserId;

import java.util.List;

public record FetchMessagesResponseRecord(
        UserId userId,
        ChannelId channelId,
        List<Message> messages
) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_MESSAGES_RESPONSE;
    }
}
