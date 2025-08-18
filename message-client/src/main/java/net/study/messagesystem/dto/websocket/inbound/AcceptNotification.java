package net.study.messagesystem.dto.websocket.inbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

@Getter
public class AcceptNotification extends BaseMessage {

    private final String username;

    public AcceptNotification(String username) {
        super(MessageType.NOTIFY_ACCEPT);
        this.username = username;
    }
}
