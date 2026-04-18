package net.study.messageconnectionflux.application.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagecommon.constant.MessageType;

@Getter
public class AcceptNotification extends BaseMessage {

    private final String username;

    public AcceptNotification(String username) {
        super(MessageType.NOTIFY_ACCEPT);
        this.username = username;
    }
}
