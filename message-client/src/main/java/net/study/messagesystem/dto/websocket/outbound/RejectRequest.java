package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

@Getter
public class RejectRequest extends BaseRequest {

    private final String username;

    public RejectRequest(String username) {
        super(MessageType.REJECT_REQUEST);
        this.username = username;
    }
}
