package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

@Getter
public class DisconnectRequest extends BaseRequest {

    private final String username;

    @JsonCreator
    public DisconnectRequest(
            @JsonProperty("username") String username
    ) {
        super(MessageType.DISCONNECT_REQUEST);
        this.username = username;
    }
}
