package net.study.messagesystem.dto.websocket.outbound;

import net.study.messagecommon.constant.MessageType;

public class KeepAliveRequest extends BaseRequest {

    public KeepAliveRequest() {
        super(MessageType.KEEP_ALIVE);
    }
}
