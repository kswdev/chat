package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

@Getter
public class MessageRequest extends BaseRequest {

    private final String username;
    private final String content;

    @JsonCreator
    public MessageRequest(
            @JsonProperty("username") String username,
            @JsonProperty("content") String content
    ) {
        super(MessageType.MESSAGE);
        this.username = username;
        this.content = content;
    }
}
