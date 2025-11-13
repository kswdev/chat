package net.study.messageconnection.dto.kafka;

import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.user.UserId;

import java.util.List;

public record CreateRequestRecord(UserId userId, String title, List<String> participantUsernames) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.CREATE_REQUEST;
    }
}
