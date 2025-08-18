package net.study.messagesystem.dto.websocket.inbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

@Getter
public class InviteNotification extends BaseMessage {

    private final String username;

    public InviteNotification(String username) {
        super(MessageType.ASK_INVITE);
        this.username = username;
    }
}
