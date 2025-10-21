package net.study.messagesystem.dto.kafka.outbound;

import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.domain.user.InviteCode;
import net.study.messagesystem.domain.user.UserId;

public record InviteResponse(UserId userId, InviteCode inviteCode, UserConnectionStatus status) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.INVITE_RESPONSE;
    }
}
