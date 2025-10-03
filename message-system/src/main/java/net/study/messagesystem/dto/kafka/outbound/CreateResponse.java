package net.study.messagesystem.dto.kafka.outbound;

import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.domain.channel.ChannelId;
import net.study.messagesystem.dto.domain.user.UserId;

public record CreateResponse(UserId userId, ChannelId channelId, String title) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.CREATE_RESPONSE;
    }
}
