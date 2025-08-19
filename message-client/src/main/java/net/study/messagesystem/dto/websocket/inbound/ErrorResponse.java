package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

@Getter
public class ErrorResponse extends BaseMessage {

    private final String message;
    private final String messageType;

    @JsonCreator
    public ErrorResponse(
            @JsonProperty("message") String message,
            @JsonProperty("messageType") String messageType
    ) {
        super(MessageType.ERROR);
        this.message = message;
        this.messageType = messageType;
    }
}
