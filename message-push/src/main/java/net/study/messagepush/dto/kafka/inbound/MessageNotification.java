package net.study.messagepush.dto.kafka.inbound;

import net.study.messagepush.constant.MessageType;
import net.study.messagepush.dto.channel.ChannelId;
import net.study.messagepush.dto.user.UserId;

public record MessageNotification(UserId userId, ChannelId channelId, String username, String content) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.NOTIFY_MESSAGE;
    }
}
