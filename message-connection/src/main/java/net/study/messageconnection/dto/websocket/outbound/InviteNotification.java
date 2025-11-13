package net.study.messageconnection.dto.websocket.outbound;

import lombok.Getter;
import net.study.messageconnection.constant.MessageType;

@Getter
public class InviteNotification extends BaseMessage {

    private final String username;

    public InviteNotification(String username) {
        super(MessageType.ASK_INVITE);
        this.username = username;
    }
}
