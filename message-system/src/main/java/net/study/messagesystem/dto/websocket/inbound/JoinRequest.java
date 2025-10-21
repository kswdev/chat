package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.user.InviteCode;

@Getter
public class JoinRequest extends BaseRequest {

    private final InviteCode inviteCode;

    @JsonCreator
    public JoinRequest(
            @JsonProperty("inviteCode") InviteCode inviteCode
    ) {
        super(MessageType.JOIN_REQUEST);
        this.inviteCode = inviteCode;
    }
}
