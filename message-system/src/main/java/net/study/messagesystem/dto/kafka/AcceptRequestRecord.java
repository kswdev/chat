package net.study.messagesystem.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messagesystem.domain.user.UserId;

public record AcceptRequestRecord(UserId userId, String username) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.ACCEPT_REQUEST;
    }
}
