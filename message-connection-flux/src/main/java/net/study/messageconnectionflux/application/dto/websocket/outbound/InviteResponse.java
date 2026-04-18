package net.study.messageconnectionflux.application.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagecommon.constant.MessageType;
import net.study.messagecommon.constant.UserConnectionStatus;
import net.study.messageconnectionflux.domain.user.InviteCode;

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
