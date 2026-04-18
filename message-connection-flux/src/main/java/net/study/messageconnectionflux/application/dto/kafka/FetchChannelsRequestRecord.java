package net.study.messageconnectionflux.application.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.domain.user.UserId;

public record FetchChannelsRequestRecord(UserId userId) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_CHANNELS_REQUEST;
    }
}
