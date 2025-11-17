package net.study.messagesystem.dto.kafka;

import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.user.InviteCode;
import net.study.messagesystem.domain.user.UserId;

public record FetchUserInviteCodeResponseRecord(UserId userId, InviteCode inviteCode) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_USER_INVITE_CODE_RESPONSE;
    }
}
