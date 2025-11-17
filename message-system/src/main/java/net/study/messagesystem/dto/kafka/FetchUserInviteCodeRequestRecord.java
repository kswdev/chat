package net.study.messagesystem.dto.kafka;

import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.user.UserId;

public record FetchUserInviteCodeRequestRecord(UserId userId) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_USER_INVITE_CODE_REQUEST;
    }
}
