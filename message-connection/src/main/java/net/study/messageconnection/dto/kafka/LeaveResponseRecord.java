package net.study.messageconnection.dto.kafka;


import net.study.messagecommon.constant.MessageType;
import net.study.messageconnection.domain.user.UserId;

public record LeaveResponseRecord(UserId userId) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.LEAVE_RESPONSE;
    }
}
