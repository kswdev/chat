package net.study.messagesystem.dto.kafka.outbound;

import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.user.UserId;

public record InviteNotification(UserId userId, String username) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.ASK_INVITE;
    }
}
