package net.study.messageconnectionflux.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.domain.user.InviteCode;
import net.study.messageconnectionflux.domain.user.UserId;

public record JoinRequestRecord(UserId userId, InviteCode inviteCode) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.JOIN_REQUEST;
    }
}
