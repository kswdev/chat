package net.study.messageconnection.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.constant.UserConnectionStatus;

@Getter
public class FetchUserConnectionsRequest extends BaseRequest {

    private final UserConnectionStatus status;

    @JsonCreator
    public FetchUserConnectionsRequest(
            @JsonProperty("status") UserConnectionStatus status
    ) {
        super(MessageType.FETCH_USER_CONNECTIONS_REQUEST);
        this.status = status;
    }
}
