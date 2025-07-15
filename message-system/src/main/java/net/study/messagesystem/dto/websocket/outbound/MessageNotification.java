package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

@Getter
public class MessageNotification extends BaseMessage{

    private final String username;
    private final String content;

    public MessageNotification(String username, String content) {
        super(MessageType.NOTIFY_MESSAGE);
        this.username = username;
        this.content = content;
    }
}
