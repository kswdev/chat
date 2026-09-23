package net.study.messageconnectionflux.application.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import net.study.messagecommon.constant.MessageType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FetchMessagesRequest.class, name = MessageType.FETCH_MESSAGES_REQUEST),
        @JsonSubTypes.Type(value = FetchUserInviteCodeRequest.class, name = MessageType.FETCH_USER_INVITE_CODE_REQUEST),
        @JsonSubTypes.Type(value = FetchUserConnectionsRequest.class, name = MessageType.FETCH_USER_CONNECTIONS_REQUEST),
        @JsonSubTypes.Type(value = AcceptRequest.class, name = MessageType.ACCEPT_REQUEST),
        @JsonSubTypes.Type(value = DisconnectRequest.class, name = MessageType.DISCONNECT_REQUEST),
        @JsonSubTypes.Type(value = RejectRequest.class, name = MessageType.REJECT_REQUEST),
        @JsonSubTypes.Type(value = InviteRequest.class, name = MessageType.INVITE_REQUEST),
        @JsonSubTypes.Type(value = WriteMessage.class, name = MessageType.WRITE_MESSAGE),
        @JsonSubTypes.Type(value = ReadMessageAck.class, name = MessageType.READ_MESSAGE_ACK),
        @JsonSubTypes.Type(value = KeepAliveRequest.class, name = MessageType.KEEP_ALIVE)
})
public abstract class BaseRequest {
    @Getter private final String type;

    public BaseRequest(String type) {
        this.type = type;
    }
}


