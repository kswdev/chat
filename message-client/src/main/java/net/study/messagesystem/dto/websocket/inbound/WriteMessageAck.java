package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.message.MessageSeqId;

@Getter
public class WriteMessageAck extends BaseMessage{

    private final MessageSeqId messageSeqId;
    private final Long serial;

    @JsonCreator
    public WriteMessageAck(
            @JsonProperty("messageSeqId") MessageSeqId messageSeqId,
            @JsonProperty("serial") Long serial
    ) {
        super(MessageType.WRITE_MESSAGE_ACK);
        this.messageSeqId = messageSeqId;
        this.serial = serial;
    }
}
