package net.study.messageconnectionflux.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messagecommon.constant.UserConnectionStatus;
import net.study.messageconnectionflux.domain.user.InviteCode;
import net.study.messageconnectionflux.domain.user.UserId;

public record InviteResponseRecord(UserId userId, InviteCode inviteCode, UserConnectionStatus status) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.INVITE_RESPONSE;
    }
}
