package net.study.messagesystem.dto.websocket.outbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

@Getter
public class CreateRequest extends BaseRequest {

    private final String title;
    private final String participantUsername;

    public CreateRequest(String title, String participantUsername) {
        super(MessageType.CREATE_REQUEST);
        this.title = title;
        this.participantUsername = participantUsername;
    }
}
