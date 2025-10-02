package net.study.messagepush.dto.kafka.inbound;

import net.study.messagepush.constant.MessageType;
import net.study.messagepush.dto.user.UserId;

public record AcceptResponse(UserId userId, String username) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.ACCEPT_RESPONSE;
    }
}
