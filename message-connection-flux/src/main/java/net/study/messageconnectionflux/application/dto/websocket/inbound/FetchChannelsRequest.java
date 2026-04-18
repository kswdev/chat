package net.study.messageconnectionflux.application.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import net.study.messagecommon.constant.MessageType;

@Getter
public class FetchChannelsRequest extends BaseRequest {

    @JsonCreator
    public FetchChannelsRequest() {
        super(MessageType.FETCH_CHANNELS_REQUEST);
    }
}
