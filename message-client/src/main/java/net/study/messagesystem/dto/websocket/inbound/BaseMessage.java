package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.websocket.outbound.InviteRequest;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = EnterResponse.class, name = MessageType.ENTER_RESPONSE),
        @JsonSubTypes.Type(value = CreateResponse.class, name = MessageType.CREATE_RESPONSE),
        @JsonSubTypes.Type(value = InviteResponse.class, name = MessageType.INVITE_RESPONSE),
        @JsonSubTypes.Type(value = AcceptResponse.class, name = MessageType.ACCEPT_RESPONSE),
        @JsonSubTypes.Type(value = RejectResponse.class, name = MessageType.REJECT_RESPONSE),
        @JsonSubTypes.Type(value = DisconnectResponse.class, name = MessageType.DISCONNECT_RESPONSE),
        @JsonSubTypes.Type(value = FetchUserConnectionsResponse.class, name = MessageType.FETCH_USER_CONNECTIONS_RESPONSE),
        @JsonSubTypes.Type(value = FetchUserInviteCodeResponse.class, name = MessageType.FETCH_USER_INVITE_CODE_RESPONSE),

        @JsonSubTypes.Type(value = JoinNotification.class, name = MessageType.NOTIFY_JOIN),
        @JsonSubTypes.Type(value = InviteNotification.class, name = MessageType.ASK_INVITE),
        @JsonSubTypes.Type(value = AcceptNotification.class, name = MessageType.NOTIFY_ACCEPT),
        @JsonSubTypes.Type(value = MessageNotification.class, name = MessageType.NOTIFY_MESSAGE),

        @JsonSubTypes.Type(value = ErrorResponse.class, name = MessageType.ERROR)
})
@Getter
@RequiredArgsConstructor
public abstract class BaseMessage {

    private final String type;
}

