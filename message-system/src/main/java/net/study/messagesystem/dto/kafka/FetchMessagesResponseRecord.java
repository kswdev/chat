package net.study.messagesystem.dto.kafka;

import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.message.Message;
import net.study.messagesystem.domain.user.UserId;

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
