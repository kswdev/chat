package net.study.messagepush.dto.kafka.inbound;

import net.study.messagepush.constant.MessageType;
import net.study.messagepush.dto.user.UserId;

public record LeaveResponse(UserId userId) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.LEAVE_RESPONSE;
    }
}
