package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.message.MessageSeqId;

@Getter
public class WriteMessageAck extends BaseMessage{

    private final MessageSeqId messageSeqId;
    private final Long serial;

    public WriteMessageAck(MessageSeqId messageSeqId, Long serial) {
        super(MessageType.WRITE_MESSAGE_ACK);
        this.messageSeqId = messageSeqId;
        this.serial = serial;
    }
}
