package net.study.messageconnectionflux.application.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagecommon.constant.MessageType;

@Getter
public class AcceptResponse extends BaseMessage {

    private final String username;

    public AcceptResponse(String username) {
        super(MessageType.ACCEPT_RESPONSE);
        this.username = username;
    }
}
