package net.study.messageconnection.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.dto.websocket.inbound.BaseRequest;

@Getter
public class RejectRequest extends BaseRequest {

    private final String username;

    @JsonCreator
    public RejectRequest(
            @JsonProperty("username") String username
    ) {
        super(MessageType.REJECT_REQUEST);
        this.username = username;
    }
}
