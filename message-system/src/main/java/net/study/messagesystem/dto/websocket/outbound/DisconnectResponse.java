package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.dto.domain.user.InviteCode;

@Getter
public class DisconnectResponse extends BaseMessage{

    private final String username;
    private final UserConnectionStatus status;

    public DisconnectResponse(String username, UserConnectionStatus status) {
        super(MessageType.DISCONNECT_RESPONSE);
        this.username = username;
        this.status = status;
    }
}
