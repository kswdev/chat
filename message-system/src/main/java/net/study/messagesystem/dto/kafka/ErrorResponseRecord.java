package net.study.messagesystem.dto.kafka;

import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.user.UserId;


public record ErrorResponseRecord(
        UserId userId,
        String messageType,
        String message
) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.ERROR;
    }
}