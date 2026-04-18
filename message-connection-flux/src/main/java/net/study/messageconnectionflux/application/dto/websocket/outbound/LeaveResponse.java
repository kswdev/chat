package net.study.messageconnectionflux.application.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagecommon.constant.MessageType;

@Getter
public class LeaveResponse extends BaseMessage {

    public LeaveResponse() {
        super(MessageType.LEAVE_RESPONSE);
    }
}
