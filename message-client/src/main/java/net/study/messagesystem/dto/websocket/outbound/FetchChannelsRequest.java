package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;

@Getter
public class FetchChannelsRequest extends BaseRequest {

    public FetchChannelsRequest() {
        super(MessageType.FETCH_CHANNELS_REQUEST);
    }
}
