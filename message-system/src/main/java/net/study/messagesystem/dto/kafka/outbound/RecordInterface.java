package net.study.messagesystem.dto.kafka.outbound;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.study.messagesystem.constant.MessageType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = JoinResponse.class, name = MessageType.JOIN_RESPONSE),
        @JsonSubTypes.Type(value = QuitResponse.class, name = MessageType.QUIT_RESPONSE),
        @JsonSubTypes.Type(value = LeaveResponse.class, name = MessageType.LEAVE_RESPONSE),
        @JsonSubTypes.Type(value = CreateResponse.class, name = MessageType.CREATE_RESPONSE),
        @JsonSubTypes.Type(value = InviteResponse.class, name = MessageType.INVITE_RESPONSE),
        @JsonSubTypes.Type(value = AcceptResponse.class, name = MessageType.ACCEPT_RESPONSE),
        @JsonSubTypes.Type(value = RejectResponse.class, name = MessageType.REJECT_RESPONSE),
        @JsonSubTypes.Type(value = DisconnectResponse.class, name = MessageType.DISCONNECT_RESPONSE),

        @JsonSubTypes.Type(value = InviteNotification.class, name = MessageType.ASK_INVITE),
        @JsonSubTypes.Type(value = JoinNotification.class, name = MessageType.NOTIFY_JOIN),
        @JsonSubTypes.Type(value = AcceptNotification.class, name = MessageType.NOTIFY_ACCEPT),
        @JsonSubTypes.Type(value = MessageNotification.class, name = MessageType.NOTIFY_MESSAGE),
})
public interface RecordInterface {
    String type();
}

