package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.connection.Connection;

import java.util.List;

@Getter
public class FetchUserConnectionsResponse extends BaseMessage{

    private final List<Connection> connections;

    @JsonCreator
    public FetchUserConnectionsResponse(
            @JsonProperty("connections") List<Connection> connections
    ) {
        super(MessageType.FETCH_USER_CONNECTIONS_RESPONSE);
        this.connections = connections;
    }
}
