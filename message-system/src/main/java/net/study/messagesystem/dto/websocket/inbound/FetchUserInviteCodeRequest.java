package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.user.InviteCode;

@Getter
public class FetchUserInviteCodeRequest extends BaseRequest {

    @JsonCreator
    public FetchUserInviteCodeRequest() {
        super(MessageType.FETCH_USER_INVITE_CODE_REQUEST);
    }
}
