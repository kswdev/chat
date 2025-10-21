package net.study.messagesystem.dto.kafka.outbound;

import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.user.UserId;

public record LeaveResponse(UserId userId) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.LEAVE_RESPONSE;
    }
}
