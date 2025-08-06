package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

@Getter
public class FetchUserInviteCodeRequest extends BaseRequest {

    @JsonCreator
    public FetchUserInviteCodeRequest() {
        super(MessageType.FETCH_USER_INVITE_CODE_REQUEST);
    }
}
