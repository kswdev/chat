package net.study.messagesystem.dto.kafka;

import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.user.UserId;


public record ErrorResponseRecord(
        UserId userId,
        String message,
        String messageType
) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.ERROR;
    }
}