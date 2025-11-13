package net.study.messageconnection.dto.kafka;

import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.constant.UserConnectionStatus;
import net.study.messageconnection.domain.user.InviteCode;
import net.study.messageconnection.domain.user.UserId;

public record InviteResponseRecord(UserId userId, InviteCode inviteCode, UserConnectionStatus status) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.INVITE_RESPONSE;
    }
}
