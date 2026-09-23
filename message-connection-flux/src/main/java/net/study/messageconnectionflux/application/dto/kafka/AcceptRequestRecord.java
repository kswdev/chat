package net.study.messageconnectionflux.application.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.domain.user.UserId;

public record AcceptRequestRecord(UserId userId, String username, String accepterUsername) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.ACCEPT_REQUEST;
    }
}
