package net.study.messagesystem.dto.kafka;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.study.messagecommon.constant.MessageType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = JoinRequestRecord.class, name = MessageType.JOIN_REQUEST),
        @JsonSubTypes.Type(value = JoinResponseRecord.class, name = MessageType.JOIN_RESPONSE),
        @JsonSubTypes.Type(value = EnterRequestRecord.class, name = MessageType.ENTER_REQUEST),
        @JsonSubTypes.Type(value = EnterResponseRecord.class, name = MessageType.ENTER_RESPONSE),
        @JsonSubTypes.Type(value = LeaveRequestRecord.class, name = MessageType.LEAVE_REQUEST),
        @JsonSubTypes.Type(value = LeaveResponseRecord.class, name = MessageType.LEAVE_RESPONSE),
        @JsonSubTypes.Type(value = QuitRequestRecord.class, name = MessageType.QUIT_REQUEST),
        @JsonSubTypes.Type(value = QuitResponseRecord.class, name = MessageType.QUIT_RESPONSE),
        @JsonSubTypes.Type(value = CreateRequestRecord.class, name = MessageType.CREATE_REQUEST),
        @JsonSubTypes.Type(value = CreateResponseRecord.class, name = MessageType.CREATE_RESPONSE),
        @JsonSubTypes.Type(value = FetchMessagesRequestRecord.class, name = MessageType.FETCH_MESSAGES_REQUEST),
        @JsonSubTypes.Type(value = FetchMessagesResponseRecord.class, name = MessageType.FETCH_MESSAGES_RESPONSE),
        @JsonSubTypes.Type(value = FetchChannelsRequestRecord.class, name = MessageType.FETCH_CHANNELS_REQUEST),
        @JsonSubTypes.Type(value = FetchChannelsResponseRecord.class, name = MessageType.FETCH_CHANNELS_RESPONSE),
        @JsonSubTypes.Type(value = FetchChannelInviteCodeRequestRecord.class, name = MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST),
        @JsonSubTypes.Type(value = FetchChannelInviteCodeResponseRecord.class, name = MessageType.FETCH_CHANNEL_INVITE_CODE_RESPONSE),

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

