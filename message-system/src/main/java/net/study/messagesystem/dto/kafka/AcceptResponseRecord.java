package net.study.messagesystem.dto.kafka;

import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.user.UserId;

public record AcceptResponseRecord(UserId userId, String username) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.ACCEPT_RESPONSE;
    }
}
