package net.study.messagesystem.dto.websocket.inbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.dto.user.InviteCode;

@Getter
public class InviteResponse extends BaseMessage{

    private final InviteCode inviteCode;
    private final UserConnectionStatus status;

    public InviteResponse(InviteCode inviteCode, UserConnectionStatus status) {
        super(MessageType.INVITE_RESPONSE);
        this.inviteCode = inviteCode;
        this.status = status;
    }
}
