package net.study.messageconnection.dto.kafka;

import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.constant.UserConnectionStatus;
import net.study.messageconnection.domain.user.UserId;

public record DisconnectResponseRecord(UserId userId, String username, UserConnectionStatus status) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.DISCONNECT_RESPONSE;
    }
}
