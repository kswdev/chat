package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

@Getter
public class MessageRequest extends BaseRequest {

    private final String username;
    private final String content;

    public MessageRequest(
            String username,
            String content
    ) {
        super(MessageType.MESSAGE);
        this.username = username;
        this.content = content;
    }
}
