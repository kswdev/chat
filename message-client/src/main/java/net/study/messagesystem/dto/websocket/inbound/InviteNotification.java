package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

@Getter
public class InviteNotification extends BaseMessage {

    private final String username;

    @JsonCreator
    public InviteNotification(
            @JsonProperty("username") String username
    ) {
        super(MessageType.ASK_INVITE);
        this.username = username;
    }
}
