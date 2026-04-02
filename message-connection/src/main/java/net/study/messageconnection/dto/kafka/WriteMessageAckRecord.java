package net.study.messageconnection.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messageconnection.domain.message.MessageSeqId;
import net.study.messageconnection.domain.user.UserId;

public record WriteMessageAckRecord(UserId userId, Long serial, MessageSeqId messageSeqId) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.WRITE_MESSAGE;
    }
}
