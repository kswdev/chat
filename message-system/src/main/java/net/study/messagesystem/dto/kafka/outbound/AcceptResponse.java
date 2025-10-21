package net.study.messagesystem.dto.kafka.outbound;

import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.user.UserId;

public record AcceptResponse(UserId userId, String username) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.ACCEPT_RESPONSE;
    }
}
