package net.study.messageconnection.dto.kafka;

import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.constant.UserConnectionStatus;
import net.study.messageconnection.domain.user.UserId;

public record FetchUserConnectionsRequestRecord(UserId userId, UserConnectionStatus status) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_USER_CONNECTIONS_REQUEST;
    }
}
