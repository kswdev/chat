package net.study.messageconnection.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messageconnection.constant.MessageType;

@Getter
public class AcceptRequest extends BaseRequest {

    private final String username;

    @JsonCreator
    public AcceptRequest(
            @JsonProperty("username") String username
    ) {
        super(MessageType.ACCEPT_REQUEST);
        this.username = username;
    }
}
