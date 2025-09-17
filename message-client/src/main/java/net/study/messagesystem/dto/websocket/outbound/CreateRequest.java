package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

import java.util.List;

@Getter
public class CreateRequest extends BaseRequest {

    private final String title;
    private final List<String> participantUsernames;

    public CreateRequest(String title, List<String> participantUsernames) {
        super(MessageType.CREATE_REQUEST);
        this.title = title;
        this.participantUsernames = participantUsernames;
    }
}
