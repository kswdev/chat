package net.study.messageconnection.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.user.InviteCode;

@Getter
public class InviteRequest extends BaseRequest {

    private final InviteCode userInviteCode;

    @JsonCreator
    public InviteRequest(
            @JsonProperty("userInviteCode") InviteCode inviteCode
    ) {
        super(MessageType.INVITE_REQUEST);
        this.userInviteCode = inviteCode;
    }
}
