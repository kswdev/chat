package net.study.messagesystem.dto.kafka;

import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.user.InviteCode;
import net.study.messagesystem.domain.user.UserId;

public record InviteRequestRecord(UserId userId, InviteCode userInviteCode) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.INVITE_REQUEST;
    }
}
