package net.study.messagesystem.dto.kafka.outbound;

import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.domain.user.UserId;

public record RejectResponse(UserId userId, String username, UserConnectionStatus status) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.REJECT_RESPONSE;
    }
}
