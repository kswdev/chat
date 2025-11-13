package net.study.messageconnection.dto.websocket.outbound;

import lombok.Getter;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.message.MessageSeqId;

@Getter
public class WriteMessageAck extends BaseMessage {

    private final MessageSeqId messageSeqId;
    private final Long serial;

    public WriteMessageAck(MessageSeqId messageSeqId, Long serial) {
        super(MessageType.WRITE_MESSAGE_ACK);
        this.messageSeqId = messageSeqId;
        this.serial = serial;
    }
}
