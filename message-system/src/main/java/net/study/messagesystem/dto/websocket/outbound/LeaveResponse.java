package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

@Getter
public class LeaveResponse extends BaseMessage{

    public LeaveResponse() {
        super(MessageType.LEAVE_RESPONSE);
    }
}
