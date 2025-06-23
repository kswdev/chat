package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import net.study.messagesystem.constant.MessageType;

public class KeepAliveRequest extends BaseRequest {

    @JsonCreator
    public KeepAliveRequest() {
        super(MessageType.KEEP_ALIVE);
    }
}
