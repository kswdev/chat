package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

@Getter
public class AcceptResponse extends BaseMessage{

    private final String username;

    @JsonCreator
    public AcceptResponse(
            @JsonProperty("username")  String username
    ) {
        super(MessageType.ACCEPT_RESPONSE);
        this.username = username;
    }
}
