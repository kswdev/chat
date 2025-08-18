package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.UserConnectionStatus;

@Getter
public class FetchUserConnectionsRequest extends BaseRequest{

    private final UserConnectionStatus status;

    public FetchUserConnectionsRequest(UserConnectionStatus status) {
        super(MessageType.FETCH_USER_CONNECTIONS_REQUEST);
        this.status = status;
    }
}
