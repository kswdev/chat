package net.study.messagesystem.dto.kafka;

import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.user.UserId;

import java.util.List;

public record CreateRequestRecord(UserId userId, String title, List<String> participantUsernames) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.CREATE_REQUEST;
    }
}
