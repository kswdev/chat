package net.study.messageconnection.dto.kafka;

import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.connection.Connection;
import net.study.messageconnection.domain.user.UserId;

import java.util.List;

public record FetchUserConnectionsResponseRecord(
        UserId userId, List<Connection> connections
) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_MESSAGES_RESPONSE;
    }
}
