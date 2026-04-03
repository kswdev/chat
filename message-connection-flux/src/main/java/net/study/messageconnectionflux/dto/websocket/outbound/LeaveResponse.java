package net.study.messageconnectionflux.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagecommon.constant.MessageType;

@Getter
public class LeaveResponse extends BaseMessage {

    public LeaveResponse() {
        super(MessageType.LEAVE_RESPONSE);
    }
}
