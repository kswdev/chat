package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

@Getter
public class LeaveRequest extends BaseRequest {

    public LeaveRequest() {
        super(MessageType.JOIN_REQUEST);
    }
}
