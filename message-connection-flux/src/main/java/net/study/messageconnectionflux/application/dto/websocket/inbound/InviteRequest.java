package net.study.messageconnectionflux.application.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.domain.user.InviteCode;

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
