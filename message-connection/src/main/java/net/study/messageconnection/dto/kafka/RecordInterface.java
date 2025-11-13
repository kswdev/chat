package net.study.messageconnection.dto.kafka;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.study.messageconnection.constant.MessageType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = JoinRequestRecord.class, name = MessageType.JOIN_REQUEST),
        @JsonSubTypes.Type(value = EnterRequestRecord.class, name = MessageType.ENTER_REQUEST),
        @JsonSubTypes.Type(value = RejectRequestRecord.class, name = MessageType.REJECT_REQUEST),
        @JsonSubTypes.Type(value = RejectResponseRecord.class, name = MessageType.REJECT_RESPONSE),
        @JsonSubTypes.Type(value = LeaveRequestRecord.class, name = MessageType.LEAVE_REQUEST),
        @JsonSubTypes.Type(value = LeaveResponseRecord.class, name = MessageType.LEAVE_RESPONSE),
        @JsonSubTypes.Type(value = QuitRequestRecord.class, name = MessageType.QUIT_REQUEST),
        @JsonSubTypes.Type(value = QuitResponseRecord.class, name = MessageType.QUIT_RESPONSE),
        @JsonSubTypes.Type(value = CreateRequestRecord.class, name = MessageType.CREATE_REQUEST),
        @JsonSubTypes.Type(value = CreateResponseRecord.class, name = MessageType.CREATE_RESPONSE),
        @JsonSubTypes.Type(value = InviteRequestRecord.class, name = MessageType.INVITE_REQUEST),
        @JsonSubTypes.Type(value = InviteResponseRecord.class, name = MessageType.INVITE_RESPONSE),
        @JsonSubTypes.Type(value = AcceptRequestRecord.class, name = MessageType.ACCEPT_REQUEST),
        @JsonSubTypes.Type(value = AcceptResponseRecord.class, name = MessageType.ACCEPT_RESPONSE),
        @JsonSubTypes.Type(value = AcceptNotificationRecord.class, name = MessageType.NOTIFY_ACCEPT),
        @JsonSubTypes.Type(value = RejectResponseRecord.class, name = MessageType.REJECT_RESPONSE),
        @JsonSubTypes.Type(value = DisconnectRequestRecord.class, name = MessageType.DISCONNECT_REQUEST),
        @JsonSubTypes.Type(value = DisconnectResponseRecord.class, name = MessageType.DISCONNECT_RESPONSE),
        @JsonSubTypes.Type(value = FetchChannelsRequestRecord.class, name = MessageType.FETCH_CHANNELS_REQUEST),
        @JsonSubTypes.Type(value = FetchChannelsRequestRecord.class, name = MessageType.FETCH_CHANNELS_REQUEST),
        @JsonSubTypes.Type(value = FetchUserInviteCodeRequestRecord.class, name = MessageType.FETCH_USER_INVITE_CODE_REQUEST),
        @JsonSubTypes.Type(value = FetchUserConnectionsRequestRecord.class, name = MessageType.FETCH_USER_CONNECTIONS_REQUEST),
        @JsonSubTypes.Type(value = FetchChannelInviteCodeRequestRecord.class, name = MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST),

        @JsonSubTypes.Type(value = InviteNotificationRecord.class, name = MessageType.ASK_INVITE),
        @JsonSubTypes.Type(value = JoinNotificationRecord.class, name = MessageType.NOTIFY_JOIN),
        @JsonSubTypes.Type(value = AcceptNotificationRecord.class, name = MessageType.NOTIFY_ACCEPT),
        @JsonSubTypes.Type(value = MessageNotificationRecord.class, name = MessageType.NOTIFY_MESSAGE),
        @JsonSubTypes.Type(value = ReadMessageAckRecord.class, name = MessageType.READ_MESSAGE_ACK),
        @JsonSubTypes.Type(value = WriteMessageRecord.class, name = MessageType.WRITE_MESSAGE),
})
public interface RecordInterface {
    String type();
}

