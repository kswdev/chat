package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.UserConnectionStatus;

@Getter
public class RejectResponse extends BaseMessage{

    private final String username;
    private final UserConnectionStatus status;

    public RejectResponse(String username, UserConnectionStatus status) {
        super(MessageType.REJECT_RESPONSE);
        this.username = username;
        this.status = status;
    }
}
