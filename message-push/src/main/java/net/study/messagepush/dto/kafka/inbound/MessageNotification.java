package net.study.messagepush.dto.kafka.inbound;

import net.study.messagepush.constant.MessageType;
import net.study.messagepush.dto.channel.ChannelId;
import net.study.messagepush.dto.message.MessageSeqId;
import net.study.messagepush.dto.user.UserId;

import java.util.List;

public record MessageNotification(
        UserId userId,
        ChannelId channelId,
        MessageSeqId messageSeqId,
        String username,
        String content,
        List<UserId> participantIds
) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.NOTIFY_MESSAGE;
    }
}
