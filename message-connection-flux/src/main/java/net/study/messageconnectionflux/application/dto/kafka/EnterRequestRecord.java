package net.study.messageconnectionflux.application.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.domain.channel.ChannelId;
import net.study.messageconnectionflux.domain.user.UserId;

public record EnterRequestRecord(UserId userId, ChannelId channelId) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.ENTER_REQUEST;
    }
}
