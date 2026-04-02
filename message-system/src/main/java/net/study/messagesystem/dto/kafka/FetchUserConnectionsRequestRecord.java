package net.study.messagesystem.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messagecommon.constant.UserConnectionStatus;
import net.study.messagesystem.domain.user.UserId;

public record FetchUserConnectionsRequestRecord(UserId userId, UserConnectionStatus status) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_USER_CONNECTIONS_REQUEST;
    }
}
