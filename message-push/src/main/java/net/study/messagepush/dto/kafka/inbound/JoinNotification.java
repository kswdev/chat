package net.study.messagepush.dto.kafka.inbound;

import net.study.messagecommon.constant.MessageType;
import net.study.messagepush.dto.channel.ChannelId;
import net.study.messagepush.dto.user.UserId;

public record JoinNotification(UserId userId, ChannelId channelId, String title) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.NOTIFY_JOIN;
    }
}
