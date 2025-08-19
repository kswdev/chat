package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.UserConnectionStatus;

@Getter
public class RejectResponse extends BaseMessage{

    private final String username;
    private final UserConnectionStatus status;

    @JsonCreator
    public RejectResponse(
            @JsonProperty("username") String username,
            @JsonProperty("status") UserConnectionStatus status) {
        super(MessageType.REJECT_RESPONSE);
        this.username = username;
        this.status = status;
    }
}
