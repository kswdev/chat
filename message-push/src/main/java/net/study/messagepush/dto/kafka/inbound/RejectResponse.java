package net.study.messagepush.dto.kafka.inbound;

import net.study.messagecommon.constant.MessageType;
import net.study.messagecommon.constant.UserConnectionStatus;
import net.study.messagepush.dto.user.UserId;

public record RejectResponse(UserId userId, String username, UserConnectionStatus status) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.REJECT_RESPONSE;
    }
}
