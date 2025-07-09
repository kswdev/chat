package net.study.messagesystem.dto.websocket.inbound;

import net.study.messagesystem.constant.MessageType;

public class KeepAliveRequest extends BaseRequest {

    public KeepAliveRequest() {
        super(MessageType.KEEP_ALIVE);
    }
}
