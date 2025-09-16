package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

@Getter
public class LeaveRequest extends BaseRequest {

    @JsonCreator
    public LeaveRequest() {
        super(MessageType.JOIN_REQUEST);
    }
}
