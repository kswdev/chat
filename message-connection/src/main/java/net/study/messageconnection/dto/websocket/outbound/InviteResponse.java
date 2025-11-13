package net.study.messageconnection.dto.websocket.outbound;

import lombok.Getter;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.constant.UserConnectionStatus;
import net.study.messageconnection.domain.user.InviteCode;

@Getter
public class InviteResponse extends BaseMessage {

    private final InviteCode inviteCode;
    private final UserConnectionStatus status;

    public InviteResponse(InviteCode inviteCode, UserConnectionStatus status) {
        super(MessageType.INVITE_RESPONSE);
        this.inviteCode = inviteCode;
        this.status = status;
    }
}
