package net.study.messagesystem.dto.kafka;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.study.messagecommon.constant.MessageType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FetchMessagesRequestRecord.class, name = MessageType.FETCH_MESSAGES_REQUEST),
        @JsonSubTypes.Type(value = FetchMessagesResponseRecord.class, name = MessageType.FETCH_MESSAGES_RESPONSE),

        @JsonSubTypes.Type(value = JoinNotificationRecord.class, name = MessageType.NOTIFY_JOIN),
        @JsonSubTypes.Type(value = MessageNotificationRecord.class, name = MessageType.NOTIFY_MESSAGE),
        @JsonSubTypes.Type(value = WriteMessageAckRecord.class, name = MessageType.WRITE_MESSAGE_ACK),
        @JsonSubTypes.Type(value = ReadMessageAckRecord.class, name = MessageType.READ_MESSAGE_ACK),
        @JsonSubTypes.Type(value = WriteMessageRecord.class, name = MessageType.WRITE_MESSAGE),
        @JsonSubTypes.Type(value = ErrorResponseRecord.class, name = MessageType.ERROR),
})
public interface RecordInterface {
    String type();
}

