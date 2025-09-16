package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.user.InviteCode;

@Getter
public class JoinRequest extends BaseRequest {

    private final InviteCode inviteCode;

    public JoinRequest(InviteCode inviteCode) {
        super(MessageType.JOIN_REQUEST);
        this.inviteCode = inviteCode;
    }
}
