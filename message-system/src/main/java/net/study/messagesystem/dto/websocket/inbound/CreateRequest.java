package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

import java.util.List;

@Getter
public class CreateRequest extends BaseRequest {

    private final String title;
    private final List<String> participantUsernames;

    @JsonCreator
    public CreateRequest(
            @JsonProperty("title") String title,
            @JsonProperty("participantUsernames") List<String> participantUsernames
    ) {
        super(MessageType.CREATE_REQUEST);
        this.title = title;
        this.participantUsernames = participantUsernames;
    }
}
