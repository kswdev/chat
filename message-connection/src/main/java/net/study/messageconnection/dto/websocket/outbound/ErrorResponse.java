package net.study.messageconnection.dto.websocket.outbound;

import lombok.Getter;
import net.study.messageconnection.constant.MessageType;

@Getter
public class ErrorResponse extends BaseMessage {

    private final String message;
    private final String messageType;

    public ErrorResponse(String message, String messageType) {
        super(MessageType.ERROR);
        this.message = message;
        this.messageType = messageType;
    }
}
