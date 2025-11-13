package net.study.messageconnection.dto.kafka;

import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.channel.ChannelId;
import net.study.messageconnection.domain.message.Message;
import net.study.messageconnection.domain.user.UserId;

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
