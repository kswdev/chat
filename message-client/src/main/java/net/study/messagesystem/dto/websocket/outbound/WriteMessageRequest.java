package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

@Getter
public class WriteMessageRequest extends BaseRequest {

    private final String username;
    private final String content;

    public WriteMessageRequest(String username, String content) {
        super(MessageType.WRITE_MESSAGE);
        this.username = username;
        this.content = content;
    }
}
