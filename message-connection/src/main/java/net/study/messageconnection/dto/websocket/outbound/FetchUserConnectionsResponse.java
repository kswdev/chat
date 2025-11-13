package net.study.messageconnection.dto.websocket.outbound;

import lombok.Getter;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.connection.Connection;

import java.util.List;

@Getter
public class FetchUserConnectionsResponse extends BaseMessage {

    private final List<Connection> connections;

    public FetchUserConnectionsResponse(List<Connection> connections) {
        super(MessageType.FETCH_USER_CONNECTIONS_RESPONSE);
        this.connections = connections;
    }
}
