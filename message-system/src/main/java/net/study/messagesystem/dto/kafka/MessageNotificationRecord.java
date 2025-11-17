package net.study.messagesystem.dto.kafka;

import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.message.MessageSeqId;
import net.study.messagesystem.domain.user.UserId;

import java.util.List;

public record MessageNotificationRecord(
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
