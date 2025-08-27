package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.domain.user.InviteCode;

@Getter
public class CreateRequest extends BaseRequest {

    private final String title;
    private final String participantUsername;

    @JsonCreator
    public CreateRequest(
            @JsonProperty("title") String title,
            @JsonProperty("participantUsername") String participantUsername
    ) {
        super(MessageType.CREATE_REQUEST);
        this.title = title;
        this.participantUsername = participantUsername;
    }
}
