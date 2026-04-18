package net.study.messageconnectionflux.application.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.domain.user.UserId;


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