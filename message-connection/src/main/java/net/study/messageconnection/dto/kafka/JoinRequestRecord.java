package net.study.messageconnection.dto.kafka;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.user.InviteCode;
import net.study.messageconnection.domain.user.UserId;

public record JoinRequestRecord(UserId userId, InviteCode inviteCode) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.JOIN_REQUEST;
    }
}
