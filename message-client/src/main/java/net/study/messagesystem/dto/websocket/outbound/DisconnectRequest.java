package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

@Getter
public class DisconnectRequest extends BaseRequest {

    private final String username;

    public DisconnectRequest(String username
    ) {
        super(MessageType.DISCONNECT_REQUEST);
        this.username = username;
    }
}
