package net.study.messageconnection.dto.websocket.outbound;

import lombok.Getter;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.user.InviteCode;

@Getter
public class FetchUserInviteCodeResponse extends BaseMessage {

    private final InviteCode inviteCode;

    public FetchUserInviteCodeResponse(InviteCode inviteCode) {
        super(MessageType.FETCH_USER_INVITE_CODE_RESPONSE);
        this.inviteCode = inviteCode;
    }
}
