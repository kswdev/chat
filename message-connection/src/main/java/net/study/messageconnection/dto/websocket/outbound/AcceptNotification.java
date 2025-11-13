package net.study.messageconnection.dto.websocket.outbound;

import lombok.Getter;
import net.study.messageconnection.constant.MessageType;

@Getter
public class AcceptNotification extends BaseMessage {

    private final String username;

    public AcceptNotification(String username) {
        super(MessageType.NOTIFY_ACCEPT);
        this.username = username;
    }
}
