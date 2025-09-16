package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

@Getter
public class LeaveResponse extends BaseMessage{

    @JsonCreator
    public LeaveResponse() {
        super(MessageType.LEAVE_RESPONSE);
    }
}
