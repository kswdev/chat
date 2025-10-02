package net.study.messagepush.dto.kafka.inbound;

import net.study.messagepush.constant.MessageType;
import net.study.messagepush.constant.UserConnectionStatus;
import net.study.messagepush.dto.user.UserId;

public record DisconnectResponse(UserId userId, String username, UserConnectionStatus status) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.DISCONNECT_RESPONSE;
    }
}
