package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagecommon.constant.MessageType;

@Getter
public class AcceptRequest extends BaseRequest {

    private final String username;

    public AcceptRequest(String username) {
        super(MessageType.ACCEPT_REQUEST);
        this.username = username;
    }
}
