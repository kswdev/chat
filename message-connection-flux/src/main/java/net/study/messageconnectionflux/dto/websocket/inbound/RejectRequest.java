package net.study.messageconnectionflux.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagecommon.constant.MessageType;

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
