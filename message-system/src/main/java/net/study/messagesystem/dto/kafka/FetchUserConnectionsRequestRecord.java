package net.study.messagesystem.dto.kafka;

import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.domain.user.UserId;

public record FetchUserConnectionsRequestRecord(UserId userId, UserConnectionStatus status) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_USER_CONNECTIONS_REQUEST;
    }
}
