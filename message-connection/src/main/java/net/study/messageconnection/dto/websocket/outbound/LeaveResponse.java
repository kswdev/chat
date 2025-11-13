package net.study.messageconnection.dto.websocket.outbound;

import lombok.Getter;
import net.study.messageconnection.constant.MessageType;

@Getter
public class LeaveResponse extends BaseMessage {

    public LeaveResponse() {
        super(MessageType.LEAVE_RESPONSE);
    }
}
