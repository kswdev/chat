package net.study.messageconnectionflux.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.domain.channel.ChannelId;
import net.study.messageconnectionflux.domain.user.UserId;

public record QuitResponseRecord(UserId userId, ChannelId channelId) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.QUIT_RESPONSE;
    }
}
