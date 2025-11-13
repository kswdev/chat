package net.study.messageconnection.dto.websocket.outbound;

import lombok.Getter;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.constant.UserConnectionStatus;

@Getter
public class RejectResponse extends BaseMessage {

    private final String username;
    private final UserConnectionStatus status;

    public RejectResponse(String username, UserConnectionStatus status) {
        super(MessageType.REJECT_RESPONSE);
        this.username = username;
        this.status = status;
    }
}
