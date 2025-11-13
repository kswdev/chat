package net.study.messageconnection.dto.kafka;

import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.user.UserId;

public record FetchUserInviteCodeRequestRecord(UserId userId) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_USER_INVITE_CODE_REQUEST;
    }
}
