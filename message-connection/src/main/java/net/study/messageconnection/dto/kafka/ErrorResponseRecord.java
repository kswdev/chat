package net.study.messageconnection.dto.kafka;

import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.user.UserId;


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