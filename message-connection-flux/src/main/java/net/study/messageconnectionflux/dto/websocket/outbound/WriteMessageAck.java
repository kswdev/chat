package net.study.messageconnectionflux.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.domain.message.MessageSeqId;

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
