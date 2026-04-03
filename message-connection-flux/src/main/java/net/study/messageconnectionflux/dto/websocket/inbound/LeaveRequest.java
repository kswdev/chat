package net.study.messageconnectionflux.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import net.study.messagecommon.constant.MessageType;

@Getter
public class LeaveRequest extends BaseRequest {

    @JsonCreator
    public LeaveRequest() {
        super(MessageType.LEAVE_REQUEST);
    }
}
