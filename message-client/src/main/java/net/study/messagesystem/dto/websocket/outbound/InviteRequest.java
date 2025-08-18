package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.user.InviteCode;

@Getter
public class InviteRequest extends BaseRequest {

    private final InviteCode userInviteCode;

    public InviteRequest(InviteCode inviteCode) {
        super(MessageType.INVITE_REQUEST);
        this.userInviteCode = inviteCode;
    }
}
