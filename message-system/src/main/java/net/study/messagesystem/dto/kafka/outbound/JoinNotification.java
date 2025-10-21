package net.study.messagesystem.dto.kafka.outbound;

import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.user.UserId;

public record JoinNotification(UserId userId, ChannelId channelId, String title) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.NOTIFY_JOIN;
    }
}
