package net.study.messageconnection.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messageconnection.domain.user.UserId;

public record FetchChannelsRequestRecord(UserId userId) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_CHANNELS_REQUEST;
    }
}
