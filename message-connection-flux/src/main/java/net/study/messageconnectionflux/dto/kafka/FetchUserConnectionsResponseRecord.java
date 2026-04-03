package net.study.messageconnectionflux.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.domain.connection.Connection;
import net.study.messageconnectionflux.domain.user.UserId;

import java.util.List;

public record FetchUserConnectionsResponseRecord(
        UserId userId, List<Connection> connections
) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_USER_CONNECTIONS_RESPONSE;
    }
}
