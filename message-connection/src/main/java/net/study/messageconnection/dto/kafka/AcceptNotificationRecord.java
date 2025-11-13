package net.study.messageconnection.dto.kafka;

import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.user.UserId;

public record AcceptNotificationRecord(UserId userId, String username) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.NOTIFY_ACCEPT;
    }
}
