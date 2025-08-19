package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

@Getter
public class AcceptNotification extends BaseMessage {

    private final String username;

    @JsonCreator
    public AcceptNotification(
            @JsonProperty("username") String username
    ) {
        super(MessageType.NOTIFY_ACCEPT);
        this.username = username;
    }
}
