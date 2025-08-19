package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.dto.user.InviteCode;

@Getter
public class InviteResponse extends BaseMessage{

    private final InviteCode inviteCode;
    private final UserConnectionStatus status;

    @JsonCreator
    public InviteResponse(
            @JsonProperty("inviteCode") InviteCode inviteCode,
            @JsonProperty("status") UserConnectionStatus status) {
        super(MessageType.INVITE_RESPONSE);
        this.inviteCode = inviteCode;
        this.status = status;
    }
}
