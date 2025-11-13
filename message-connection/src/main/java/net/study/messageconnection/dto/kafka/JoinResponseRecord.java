package net.study.messageconnection.dto.kafka;

import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.channel.ChannelId;
import net.study.messageconnection.domain.user.UserId;

public record JoinResponseRecord(UserId userId, ChannelId channelId, String title) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.JOIN_RESPONSE;
    }
}
