package net.study.messagesystem.dto.kafka;

import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.connection.Connection;
import net.study.messagesystem.domain.user.UserId;

import java.util.List;

public record FetchUserConnectionsResponseRecord(
        UserId userId, List<Connection> connections
) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_MESSAGES_RESPONSE;
    }
}
