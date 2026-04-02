package net.study.messageconnection.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messageconnection.domain.user.UserId;

public record DisconnectRequestRecord(UserId userId, String username) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.DISCONNECT_REQUEST;
    }
}
