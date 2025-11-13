package net.study.messageconnection.dto.websocket.outbound;

import lombok.Getter;
import net.study.messageconnection.constant.MessageType;

@Getter
public class AcceptResponse extends BaseMessage {

    private final String username;

    public AcceptResponse(String username) {
        super(MessageType.ACCEPT_RESPONSE);
        this.username = username;
    }
}
