package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.domain.user.InviteCode;

@Getter
public class FetchUserInviteCodeResponse extends BaseMessage{

    private final InviteCode inviteCode;

    public FetchUserInviteCodeResponse(InviteCode inviteCode) {
        super(MessageType.FETCH_USER_INVITE_CODE_RESPONSE);
        this.inviteCode = inviteCode;
    }
}
