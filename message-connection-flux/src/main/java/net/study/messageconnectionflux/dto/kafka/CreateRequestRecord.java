package net.study.messageconnectionflux.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.domain.user.UserId;

import java.util.List;

public record CreateRequestRecord(UserId userId, String title, List<String> participantUsernames) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.CREATE_REQUEST;
    }
}
