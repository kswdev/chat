package net.study.messageconnectionflux.application.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messagecommon.constant.UserConnectionStatus;
import net.study.messageconnectionflux.domain.user.UserId;

public record RejectResponseRecord(UserId userId, String username, UserConnectionStatus status) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.REJECT_RESPONSE;
    }
}
