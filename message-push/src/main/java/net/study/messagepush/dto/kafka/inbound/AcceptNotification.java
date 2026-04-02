package net.study.messagepush.dto.kafka.inbound;

import net.study.messagecommon.constant.MessageType;
import net.study.messagepush.dto.user.UserId;

public record AcceptNotification(UserId userId, String username) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.NOTIFY_ACCEPT;
    }
}
